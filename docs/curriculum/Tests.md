# Tests

Temporal tests can be as broad or granular as you need to fit your style and organizational test requirements. 

They also happen to be the very best way to learn about how to use Temporal primitives and experiment with patterns that reveal themselves while designing your Use Case.

### About this guidance

_State and/or Behavior Verification_

This document presumes an acquaintance with the distinction between `state` and `behavior` verification styles.
It also assumes some familiarity with the use of "Test Doubles" to isolate tests.


**Test Doubles**

This document will use the following definitions for the various [Test Double](https://martinfowler.com/bliki/TestDouble.html) types (from [Meszaros](https://www.amazon.com/gp/product/0131495054)):

* **Dummy** objects are passed around but never actually used. Usually they are just used to fill parameter lists.
* **Fake** objects actually have working implementations, but usually take some shortcut which makes them not suitable for production (an in memory database is a good example).
* **Stubs** provide canned answers to calls made during the test, usually not responding at all to anything outside what's programmed in for the test.
* **Spies** are stubs that also record some information based on how they were called. One form of this might be an email service that records how many messages it was sent.
* **Mocks** are objects pre-programmed with expectations which form a specification of the calls they are expected to receive.

### Activity Tests

>Should I test my Activities in isolation? 

Before diving into _how_ to test Activities, it is helpful to consider if tests need to be tested in isolation:

`If` the `Activity` implementation is a thin [Adapter](https://en.wikipedia.org/wiki/Adapter_pattern) that lightly transforms
input or simply exists to conform to Temporal serializable input requirements, an Activity test could be considered ceremonial/useless.

`Else if` an `Activity` has a fairly complex algorithm, an Activity test is reasonable to maintain - but only if that complexity
does not belong inside the dependency implementation itself being invoked, if any. Don't forget that Failure types 

`Else if` an `Activity` needs interacts with the `ActivityContext`, perhaps emitting periodic **heartbeats**, an Activity test
should be considered. 
> NOTE: Temporal SDK has an `ActivityTestEnvironment` which stubs an `ActivityContext` for use in Activity tests.

If you decide to test your Activity in isolation, here are some considerations based on the verification style you are doing.

#### State Verification

Activity implementations tend to be stateless, so State verification is typically done by assertion on the Activity **result**.
You can use a _stub_ to swap out dependencies instances based on setup rules.

#### Behavior Verification

Behavioral verification of an Activity is the same as testing any other function. 
If you are mocking behavior for dependencies in the Activity, obtain the dependency through Service Locator or Dependency Injection as you ordinarily would
for your verifications.

### Workflow Tests

#### State Verification

Workflows typically expose internal _State_ by either:
* Returning a result
* A response to a named `Query` handler
* A response to a named `Update` handler
* A value provided to a custom `SearchAttribute`

Therefore, the `verify` stage of a test can employ these to meet "State based" test requirements; that is,
after the Workflow is exercised, one of these methods can be used to verify its internal state. 
Note that returning a `result` from a Workflow can (and will) couple callers to that Workflow contract and 
can cause maintenance issues later. 

#### Behavior Verification

Workflows are by definition composed of behaviors, "steps", that represent atomic actions. 
Furthermore, the steps within a definition commonly receive inputs based on previous steps. 
This means it is rare to see tests that do not verify the _Behavior_ requirements in the Workflow Implementation.
Some Workflow types *only* require behavioral verification since exposing internal state is not needed; as in an Extract-Transform-Load (ETL) pipeline.

This behavioral verification requirement commonly raises the question about the appropriate components to Mock
to keep tests from becoming too brittle. This leads to a decision on the boundary for Workflow functional test isolation.

#### Workflow Test Isolation

Should `Activities` be mocked; or should the dependency(ies) _inside_ the `Activities` be mocked instead?

The boundary for your Workflow functional tests can be determined by these considerations:

_Do you intend to maintain Activity tests?_
If not, mocking or stubbing the inner dependencies these Activities use is reasonable. 
This is more compelling if you have already invested in mocks for these dependencies elsewhere.
This makes your tests "blackbox" tests where you consider the `Activity` an adapter that belongs to the Workflow definition, not the business domain; 
that is, the only reason an "Activity" component even exists is because the orchestration requires the behavior the Activity encompasses.

_Does your Workflow definition have conditional branches?_ 
If so, mocking or stubbing the Activity definitions themselves can reduce test complexity by directly providing their results, allowing you to verify the branch paths.

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

How these tests are implemented is based on your Version Control System (VCS) methodology and isolation boundaries present in your
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





