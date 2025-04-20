# Versions

## Goals

* Address message versioning strategies 
* Compare the implications of choosing _Patched_ Workflow Versioning to _Routed_ Workflow Versioning
* Introduce `Replay Testing` to safely introduce new features under load for _patched_ code branch versioning

## Message Versioning

Temporal saves in its event history the serialized representation of your messages used for inputs and outputs of all interactions with a Workflow.
Inadvertently introducing changes to the messages which are the targets of this deserialized payload data in Temporal will cripple
your Applications. 

You can avoid this by:
* Implement an explicit message version strategy that keeps messages backwards compatible.
* Hook directly into Jackson [VersionedModelConverter](https://jonpeterson.github.io/docs/jackson-module-model-versioning/1.1.1/index.html?com/github/jonpeterson/jackson/module/versioning/JsonVersionedModel.html) to control serialization/deserialization.
* Implement a [Custom Data Converter](https://docs.temporal.io/dataconversion#custom-data-converter), specifically a [PayloadConverter](https://www.javadoc.io/doc/io.temporal/temporal-sdk/latest/io/temporal/common/converter/PayloadConverter.html), that can upgrade legacy message schema to the new representation.

#### Recommendations

* Prefer keeping your messages in a discrete package that is versioned independently. 
  * This makes it easier to detect changes to the contracts impacting your Workflow Executions.
* Prefer an evolutionary, additive approach to message changes. 
  * For example, instead of _changing_ a property name prefer _adding_ the new name you want to use and keep the other around until you can deprecate it safely.
* Always use Replay tests to verify your current message schema is compatible with currently open Workflow Executions.

##### Resources

* Compare [Avro versus Protobufs](https://blog.softwaremill.com/the-best-serialization-strategy-for-event-sourcing-9321c299632b)
* Compare [Avro versus Protobufs](https://softwaremill.com/data-serialization-tools-comparison-avro-vs-protobuf/)

## Workflow Versioning

The Temporal programming model does not impose many requirements on how to build your Application features, but the one requirement which must
_always_ be enforced is **deterministic Workflow Definitions**. 

How can we deploy new features or alter behavior in our Workflow Definitions in a safe, backwards-compatible way without introducing too much complexity?

#### High Level Recommendation: Restrict Workflow Execution Lifetimes

We recommend that you frequently use `ContinueAsNew` in Workflow Executions that  are "long-running".
This frequency can largely be determined by how often you ship new features.
For example, if you typically ship new code every day and have Executions that stay `open` for a year
_and_ you need to see changes reflected in those Executions when they are shipped, devise a logical rule in your Execution that calls `ContinueAsNew` for those executions every day.

### Strategy Types

Workflow Version strategies fall into two broad categories:

* **Patched** Versioning: Wraps existing Workflow Definition with the `GetVersion` patch api usage. 
  * This strategy only requires changes to your Workflow Definitions and is covered in depth [here](https://docs.temporal.io/develop/java/versioning#patching).
* **Routed** Versioning: Deploys versions of the same Workflow Definition in parallel, segregated by either _Workflow Type_ or _Task Queue_.
  * This strategy requires updates to the "Starters" code that starts new Workflow Executions, using either the new Workflow Type or Task Queue target to run on the latest. 

Which strategy is best for you? _Here are some criteria you can use to decide:_

#### I only need to make a simple or few changes to my Workflow Definition

For example, you are just introducing a new Activity or want to change the duration of a Timer and your code is not in a loop.

**Recommendation:** _Use the Patched Strategy to make changes inside corresponding conditional logic._

#### It is simple to coordinate changes and deployment of both the Starter and Worker services

For example, you have a complex change to roll out and your team is a full-stack crew that manages both the Starters and Workers along with their
deployments.

**Recommendation**: _Use the _Routed_ strategy._ You can update your Starter code to target the new Workflow Definition after you have deployed the new
version on a separate TaskQueue or with a distinct WorkflowType name. When older versions are drained off, you can shut those Workers down
or deregister the old Workflow Types.

#### It is not simple to make changes or coordinate deployment of Starter and Worker services

For example, your frontend team might not be able to ship as frequently but you need to make changes as soon as possible.

**Recommendation**: _Use the Patched strategy_. If the changeset is complex or your Workflow definition is cognitively cumbersome,
consider pairing that with **Continue As New** to remove some of the older code branches.

## Workflow Versioning: Patched Strategy

See the [JavaDocs for API details](https://www.javadoc.io/doc/io.temporal/temporal-sdk/latest/io/temporal/workflow/Workflow.html#getVersion(java.lang.String,int,int)).

The `patch` or `GetVersion` APIs are all about safely deploying _future_ code paths during Replay by inserting a Version marker into event history. 
These `Version` markers are identified by a unique `changeId` with an Integer for comparison. 

Additionally, for all SDKs [except Java](#user-content-temporalchangeversion-search-attribute),
a SearchAttribute named `TemporalChangeVersion` is also Upserted for the `changeId` value to ease discovery of Versions that you might
remove from the codebase.

### `GetVersion` Usage

##### ;TLDR
* **Can I rename a `changeId`?**
  * No. This will cause NDE for Replaying executions.
* **Should I either update the `maxVersion` for an existing `changeId`, or mint a new `changeId` Version starting at `1`?**
  * Update the `maxVersion` if you have named the `changeId` for the feature block the change impacts.
    * This approach creates fewer `Version` markers in history.
  * If the patch is very specific, like a `bugfix` change, you should just create a new Version with its own `changeId`
* **When would I provide a `minVersion` other than `DEFAULT_VERSION`** ? 
  * Only when you need to protect old executions from accidentally running code that is no longer valid.

#### Arguments

##### changeId

A `changeId` MUST be unique across the entire Workflow Execution and should be considered immutable.
In general, never re-use a `changeId` though you could do so with a `ContinueAsNew` that clears previous history.
`changeId` might be named for the feature block it represents; for example, `payment-authorization`.
Or, it might carry more specific context, like `hotfix-JIRA-123`.

How you name the `changeId` in longer running Workflows will impact how `maxVersion` is utilized
later on.


#### maxVersion

The `maxVersion` argument can be confusing and might appear a misnomer since the same `GetVersion` invocation
either writes OR reads Execution history depending on whether the Execution is Replaying or not.

1. A `GetVersion` call encountered _the first time_ by an Execution (by Workflow ID) for a given `changeId` _writes_ the `maxVersion` value to history **as that Version**.
2. Subsequent **Replays** of that Workflow Execution line of code will _read_ that `maxVersion` value from history going forward.

This argument will usually only be incremented if the `changeId` represents a feature block of code since the 
feature might evolve over time (e.g., hotfixes do not evolve).

#### minVersion

The `minVersion` argument, when higher than `Workflow.DEFAULT_VERSION`, acts as a guard clause against old Executions that might Replay
against code that has since had earlier Versions of that change removed. 
It does this by specifying the lowest version the current code impacted by the `changeId` can support. 
An explicit `UnsupportedVersion` Exception will be raised to exit the Replay.

The `minVersion` argument is _irrelevant_ for new Executions, so `Workflow.DEFAULT_VERSION` is most often used as a Null argument.

Therefore, `minVersion` applies to Executions under Replay, while `maxVersion` applies to *future* Executions.

### Patched Versioning In Practice

Here are a few things you should consider if you decide to leverage the _Patched_ strategy.

#### _TemporalChangeVersion Search Attribute_
Most Temporal SDK's provide a `TemporalChangeVersion` Search Attribute "out-of-the-box" to
aid discovery of Patches that can be deprecated.

The Java SDK [does not provide this Search Attribute](https://github.com/temporalio/sdk-java/issues/587).
Until this is provided, you can reference [this sample](https://github.com/temporalio/samples-java/tree/main/core/src/main/java/io/temporal/samples/customchangeversion)
to learn how to publish a similar attribute for this discovery.

#### _Removing Old Versions_

* Note the gap [here](#user-content-temporalchangeversion-search-attribute)
* If there is a `Query` handler in the Workflow Definition, care should be taken in removing unused Version
blocks before the Namespace retention policy since the revealed state should be reflective
of the Version that produced it. If there is any exposed state in a Query that is computed
variously based on Version, consider waiting to remove impacted Version blocks until
the Namespace retention policy has passed.

#### _Loops_
If you introduce new code inside a loop construct, you must decide whether you need to Version:
* the whole loop: wherein the entire loop uses the result from the same `changeId`
* per iteration: wherein each iteration has a unique `changeId`

This is based on _how_ currently `Open` Workflow Executions should behave when they encounter your code change.

If the logic that applies when an Execution was started should not ever change, just Version the whole loop with the same `changeId`.
Otherwise, mint a unique `changeId` per iteration; for example, suffix the `changeId` with an index.

> **CAUTION**: If the loop will iterate many times (eg more than a thousand times), 
> the Execution history will explode with `Version` markers.
> 
> You can mitigate this risk by performing `ContinueAsNew` periodically.

#### _Rolling Deployments With Patched Workflow Definitions_

If you perform rolling deployments of Worker services that register Patched Worflow Definitions, 
it is _possible_ that a race can occur when:

1. The **latest** Patched Workflow Definition 
   1. Starts an Execution 
   2. Writes the `Version` marker to history
   3. Crashes
2. The **previous** Version
   1. Picks up the Workflow Task to resume the Execution
   2. The Task fails with a `NonDeterminismException` (NDE)

Your Workflow will not be corrupted, but you could see an impact on performance and Task
failures appear in the history for this Execution.

One mitigation of this would be to always enforce Versioning for the entire Workflow Definition before any deployment, as 
proposed [here](#user-content-global-workflow-versioning).

#### _Global Workflow Versioning_
Some users do "Global Versioning" with Workflow definitions wherein only new Executions receive the new behavior
you must Version ([source](https://medium.com/@qlong/how-to-overcome-some-maintenance-challenges-of-temporal-cadence-workflow-versioning-f893815dd18d)).
This works if you do not have long-running Workflows that need to pick up changes after a blocking Awaitable(eg, `Timer` usage). 
For example, you might have a "Subscription" Workflow that sleeps for three months and performs an Activity upon `TimerFired`.
If you want to add an Activity post-timer, you likely want to pick up this new Activity - even in Workflows that have been
`Open` for some time. This likely means falling back to using `GetVersion` directly or using **ContinueAsNew** to force a new
execution with input parameters that reflect what work has already been completed.

## Workflow Versioning: Routed Strategy

You can target specific Versions of your Workflow Definition from the `Starter` by either:
1. Specifying the WorkflowType explicitly; eg `MyWorkflowV2`
2. Designating a TaskQueue for where the new Workflow Version is hosted; eg `TaskQueue: "PaymentsV2"`

Both strategies will avoid the risk of **NonDeterminismExceptions** in currently Open Workflows.
There are a few subtle differences to consider to decide which is suitable.

#### WorkflowType Routed Versioning

This type of Versioning appends a Version into the Implementation name; eg `MyWorkflowV2`.
This is by convention only and is not enforced.

_Pros_

* Does not demand changes to your Worker deployment.
* Allows for discrete changes to each Workflow Type within the Application.

_Cons_
* Starters must update `Start/Execute` Workflow code to route to the new Implementation, possibly forcing cross-team deployments.
* Requires custom **SearchAttribute** or metrics to determine when the old Implementations can be deregistered.
* Changing definition behavior results in a whole new source file, making Git diffs exposing the changes more difficult to spot in PRs.


##### Removing Old WorkflowType Versions

Simply remove the registration of the WorkflowType from relevant Workers.

#### TaskQueue Routed Versioning

_Pros_

* Might be easier to determine which Version of your application to decommission by observing Worker compute resources.
* Explicit Version support at a higher Application level since multiple Workflow Types can be registered at once.

_Cons_

* Starters must update `Start/Execute` Workflow code to route to the new Implementation, possibly forcing cross-team deployments.
* Worker count will increase exponentially, increasing complexity in infrastructure deployments and observability.
  * This is further exacerbated if using `TaskQueue` for multi-tenancy purposes.

##### Removing Old TaskQueue Versions

If you have multiple WorkerFactories (Java) in a process, remove these factories
using the deprecated TaskQueue. If a single WorkerFactory is in a process, remove it from
the deployment.

## Replay Tests

Recall that Workflow Executions are "replayed" at various times during your Application lifetime.

> This means we must verify that the state and behaviors meet expectations regardless of **how** it is executed.

Your Workflow could build, pass all unit and functional tests, and ship to production just fine, 
but still bring your business to a halt because of Non-Determinism Errors.
This incident will be now exacerbated by the _new_ Workflow Executions that have started in production while the previous versions were "stuck".

The Temporal [WorkflowReplayer](https://docs.temporal.io/develop/java/testing-suite#replay) test facility is what you want to use to either verify or validate any production Workflow
executions in your development or build pipeline.

These types of tests are a variant of "smoke" or "build validation" tests, so run these tests where you are currently performing these other tests; 
likely this is a step just before running your other unit or functional tests.
Alternately, you can run these tests as a *verification* step and might put these in the developer code commit workflow as early as a `git commit`.

Regardless of your environment or where in the delivery process these appear, they are an important investment to your production quality.

#### Recommendations

Replay tests should exercise history that was produced by various code paths through your Executions.
If your Workflow Definition has conditional branches, loops, or timers, it makes sense to store histories created by the unit test cases that verify these scenarios with the
execution histories _caused_ by the scenarios in the `latest` build so that they might be validated in `vNext` implementations.

Factors which determine how these tests are executed are based on your Workflow Version strategy and isolation boundaries present in your
Continuous Integration / Continuous Deployment (CICD) pipeline.


#### Implementing Replay Tests : Patched Version Strategy

If you elect to use the `GetVersion` or `patch` SDK APIs, you must choose between loading Workflow History representing the
`latest` build from memory or from file storage.

**External Workflow History Storage**

* _Cons_
    * This can be problematic in ephemeral containers used for CICD purposes.
    * This might require non-trivial authnz to an external file storage solution.
    * Garbage collection on histories might be needed to keep storage costs down
        * Detecting which histories are eligible for collection can be complex for Workflow Definitions that are long-running and do not implement Continue As New
    * The `DataConverter` used to generate the histories _must_ be used when doing these tests. 
* _Pros_
    * No need to maintain more than one implementation in VCS for the same Workflow Type in the same source version.
    * Standard VCS comparison techniques are preserved in diffs to make it easy to see what changes were made.

**In-Memory Workflow History**

* _Cons_
    * Registration of the Workflow Type implementation must be preserved carefully, possibly using the Workflow Options that allow a `string` Workflow Type name.
        * (see note on Worker registration considerations below)
    * Duplicate implementations of same representation in same VCS version might philosophically contradict your organization VCS principles.
        * Placing a version in the Workflow Type name "hides" implementation changes in git comparisons by breaking helpful commit diffs during code review.
        * The git concern can be mitigated with a descending versioning scheme, where the `vNext` overwrites the `latest` while preserving the same filename.
    * Long-running workflows that do not use ContinueAsNew will keep their history around for a while so quite old implementations will need to survive in VCS to be validated against the proposed `vNext` implementation.
* _Pros_
    * No need to maintain external storage solution for the history produced by `latest` build. Validation can be done via in-memory history produced in unit tests.

> The Patched Versioning strategy is convenient because `Starters` don't need to update their code with version changes or coordinate deployments with services hosting Temporal Workers.
>
> Note that in all SDKs except Java need to specify a `string` WorkflowType name explicitly in the Worker registration,type decorator or attribute options
to "pin" the Type name to maintain this implementation swap.

## Onboardings

### Refactorings

