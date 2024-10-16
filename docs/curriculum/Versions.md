# Versions

## Goals

* Address message versioning strategies 
* Compare the implications of choosing _In Situ_ Workflow Versioning to _Routed_ Workflow Versioning
* Introduce `Replay Testing` to safely introduce new features under load for _in situ_ code branch versioning

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

## Workflow Versioning

The Temporal programming model does not impose many requirements on how to build your Application features, but the one requirement which must
_always_ be enforced is **deterministic Workflow Definitions**. 

How can we deploy new features or alter behavior in our Workflow Definitions in a safe, backwards-compatible way without introducing too much complexity?

Workflow Version strategies fall into two broad categories:

* **In Situ** Versioning: Wraps existing Workflow Definition with the `GetVersion` patch api usage. 
  * This strategy only requires changes to your Workflow Definitions and is covered in depth [here](https://docs.temporal.io/develop/java/versioning#patching).
* **Routed** Versioning: Deploys versions of the same Workflow Definition in parallel, segregated by either _Workflow Type_ or _Task Queue_.
  * This strategy requires updates to the "Starters" code that starts new Workflow Executions, using either the new Workflow Type or Task Queue target to run on the latest. 

### In Situ Versioning Gotchas

There are a few things you must be careful about to leverage the _In Situ_ strategy.

#### _Do-it-yourself Version Search Attribute_
The Java SDK [does not provide a Search Attribute](https://github.com/temporalio/sdk-java/issues/587) "out-of-the-box"
to search for Versions which are not used. Until this is patched, you can reference [this sample](https://github.com/temporalio/samples-java/tree/main/core/src/main/java/io/temporal/samples/customchangeversion)
to learn how to publish a similar attribute for this discovery.

#### _Global Workflow Versioning_
Some maintain a "Global Versioning" can be done with Workflow definitions wherein only new Executions receive the new behavior
you must Version ([source](https://medium.com/@qlong/how-to-overcome-some-maintenance-challenges-of-temporal-cadence-workflow-versioning-f893815dd18d)).
This works if you do not have long-running Workflows that want to pick up changes after a `Timer` usage. 
For example, you might have a "Subscription" Workflow that sleeps for three months and performs an Activity upon `TimerFired`.
If you want to add an Activity post-timer, you likely want to pick up this new Activity - even in Workflows that have been
`Open` for some time. This likely means falling back to the using `GetVersion` directly or using **ContinueAsNew** to force a new
execution with input parameters that reflect what work has already been completed.

#### _Loops_

If you introduce new code inside a loop construct, each iteration of that loop must wrap the new code inside a `GetVersion` call.


#### _`changeId`_ Workflow Execution Uniqueness

A `changeId` MUST be unique across the entire Workflow Execution and should be considered immutable. 
Therefore, each deployment of a Workflow implementation should all use the same `changeId`. 

Note that altering a `changeId` will throw a non-deterministic Exception (NDE). 

Which strategy is best for you?

_Here are some criteria you can use to decide:_

#### I only need to make a simple or few changes to my Workflow Definition

For example, you are just introducing a new Activity or want to change the duration of a Timer and your code is not in a loop. 
Use the `GetVersion` API _In Situ_ to make changes inside corresponding conditional logic. 

#### It is simple to coordinate changes and deployment of both the Starter and Worker services

For example, you have a complex change to roll out and your team is a full-stack crew that manages both the Starters and Workers along with their
deployments.
Use the _Routed_ strategy. You can update your Starter code to target the new Workflow Definition after you have deployed the new 
version on a separate TaskQueue or with a distinct WorkflowType name. When older versions are drained off, you can shut those Workers down 
or deregister the old Workflow Types.

#### It is not simple to make changes or coordinate deployment of Starter and Worker services

For example, your frontend team might not be able to ship as frequently but you need to make changes as soon as possible. 
Use the _In Situ_ strategy. If the changeset is complex or your Workflow definition is cognitively cumbersome, 
consider pairing that with **Continue As New** to remove some of the older code branches.



## Replay Tests

Recall that Workflow Executions are "replayed" at various times during your Application lifetime.

> This means we must verify that the state and behaviors meet expectations regardless of **how** it is executed.

Your Workflow can build, pass all unit and functional tests, and ship to production just fine, but still bring your business to a halt because of Non-Determinism Errors.
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


#### Implementing Replay Tests : In Situ Version Strategy

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
    * Duplicate implementations of same representation in same VCS version philosophically contradicts VCS principles to some.
        * Placing a version in the Workflow Type name "hides" implementation changes in git comparisons by breaking helpful commit diffs during code review.
        * The git concern can be mitigated with a descending versioning scheme, where the `vNext` overwrites the `latest` while preserving the same filename.
    * Long-running workflows that do not use ContinueAsNew will keep their history around for a while so quite old implementations will need to survive in VCS to be validated against the proposed `vNext` implementation.
* _Pros_
    * No need to maintain external storage solution for the history produced by `latest` build. Validation can be done via in-memory history produced in unit tests.

> The in situ Versioning strategy is convenient because `Starters` don't need to update their code with version changes or coordinate deployments with services hosting Temporal Workers.
>
> Note that in all SDKs except Java need to specify a `string` WorkflowType name explicitly in the Worker registration,type decorator or attribute options
to "pin" the Type name to maintain this implementation swap.



## Onboardings

### Refactorings

