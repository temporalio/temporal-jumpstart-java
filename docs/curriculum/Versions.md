# Versions

### Goals

* Address message versioning strategies 
* Compare the implications of choosing _In Situ_ Workflow Versioning to _Routed_ Workflow Versioning
* Introduce `Replay Testing` to safely introduce new features under load for _in situ_ code branch versioning

### Message Versioning

Temporal saves in its event history the serialized representation of your messages used for inputs and outputs of all interactions with a Workflow.
Inadvertently introducing changes to the messages which are the targets of this deserialized payload data in Temporal will cripple
your Applications. 

You can avoid this by:
* Implement an explicit message version strategy that keeps messages backwards compatible.
* Hook directly into Jackson [VersionedModelConverter](https://jonpeterson.github.io/docs/jackson-module-model-versioning/1.1.1/index.html?com/github/jonpeterson/jackson/module/versioning/JsonVersionedModel.html) to control serialization/deserialization.
* Implement a [Custom Data Converter](https://docs.temporal.io/dataconversion#custom-data-converter) , specifically a [PayloadConverter](https://www.javadoc.io/doc/io.temporal/temporal-sdk/latest/io/temporal/common/converter/PayloadConverter.html), that can upgrade legacy message schema to the new representation.

#### Recommendations

* Prefer keeping your messages in a discrete package that is versioned independently. 
  * This makes it easier to detect changes to the contracts impacting your Workflow Executions.
* Prefer an evolutionary, additive approach to message changes. 
  * For example, instead of _changing_ a property name prefer _adding_ the new name you want to use and keep the other around until you can deprecate it safely.
* Always use Replay tests to verify your current message schema is compatible with currently open Workflow Executions.


### Replay Tests

Recall that Temporal Workflow definitions must be non-deterministic.

Also recall that Workflows are "replayed" at various times during your Application lifetime.

> This means we must verify that the state and behaviors meet expectations regardless of **how** it is executed.

Your Workflow can build, pass all unit and functional tests, and ship to production just fine, but still bring your business to a halt because of Non-Determinism Errors.
This incident will be now exacerbated by the _new_ Workflow Executions that have started in production while the previous versions were "stuck".

The Temporal [WorkflowReplayer](https://docs.temporal.io/develop/java/testing-suite#replay) test facility is what you want to use to either verify or validate any production Workflow
executions in your development or build pipeline.

These types of tests are effectively a variant of "smoke" or "build validation" tests, so plug these tests in where you are currently performing these other tests; likely this
is a step just before running your other unit or functional tests.
You might also consider these tests *verification* tests and might put these in the developer workflow as early as a `git commit`.

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

