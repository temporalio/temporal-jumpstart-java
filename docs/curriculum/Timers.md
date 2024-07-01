# Timers

The Temporal programming model introduces a superpower by making it simple to manipulate _when_
an operation should happen. This simplicity can make a developer fail to reason about the implications
of Time and how the Tasks which are guaranteed to be delivered will impact predictable outcomes.

Before diving into the matter of "Time" with Temporal Workflow code, be sure you have read over
<The Java Thing about Non-Determinism during concurrent ops and Time>

### Gotchas

#### Deterministic Time

Be sure you understand the implications Timer's have on Workflow determinism.
Changing timers after deployment can be a subtle source of Non-Determinism Errors:
> The delay value can be Infinite or InfiniteTimeSpan but otherwise cannot be negative. A server-side timer is not created for infinite delays, so it is non-deterministic to change a timer to/from infinite from/to an actual value.

While some SDKs ignore zero timers, not writing a server-side timer, .NET does this:
> If the delay is 0, it is assumed to be 1 millisecond and still results in a server-side timer. Since Temporal timers are server-side, timer resolution may not end up as precise as system timers.

Reference [Docs](https://dotnet.temporal.io/api/Temporalio.Workflows.Workflow.html#Temporalio_Workflows_Workflow_DelayAsync_System_TimeSpan_System_Nullable_System_Threading_CancellationToken___remarks).

#### Dynamic duration

Dynamic duration values (_eg_, values received from an Activity or unvalidated input arguments) can sneak in causing
non-determinism errors or surprising results.

_Be sure you place assertions/guards on durations before using dynamic values in Timers._

#### What time is it?

Note that Timers are stored as events on the Temporal service, but if you do not have Workers running when the
Timer "fires" (meets the threshold), that TimerFired task cannot be delivered until a Worker polls for it.
If you have activities that must be executed within a certain time-frame, you should keep in mind that service
outages could lead to invalid operations due to Timers firing "late".

#### Million Timers Issue

If you schedule a Timer to fire at a fixed time across an enormous number of Workflows
(a situation we sometimes refer to as the "million timer issue"), you
could overwhelm your own resources trying to handle such a large number of Tasks.

Evaluate your use case more closely and determine whether such requirements need to be
so strictly applied. If you determine they do, consider adding "jitter" to your delay
duration to avoid bottlenecks that could compromise the business requirement fulfillment due to a backlog.

## Testing

How can we test something that is supposed to happen in the future?

## Refactorings To Our Onboarding Use Case

Our requirements demand we "wait" for approval before registering our entity with our CRM.
The onboarding must be completed within _seven days_ of submission. Let's wait that long before
exiting the Workflow as `Failed`. Here are steps we will take to model this safely:

* Introduce an explicit `state` object we can use as a private variable inside our Workflow to track the progress of various changes in our Workflow
    * Here, we are going to wait for `IsApproved` to be flipped to `true` before proceeding
* Extend our `OnboardEntityRequest` to accept an optional input for the `timeout` of approval
    * Why not use `WorkflowExecutionTimeout`?
        * Primarily because we want to express this as a _business rule_. We may want to extend this time later and we want to allow the workflow, not the caller, to govern execution rules.
        * Note that an approval could come in very late and while the activities that should complete are running they could be terminated due to the execution timeout. This would not be the intent of any time threshold.
    * Why not use a global constant for this delay timeout?
        * You can, but this will force a redeploy to make changes to product rules. We chose an optional input parameter with a fallback value to increase flexibility.
    * What about current tests that were built without this timeout constraint? There are two options:
        * We could use a "magic number" of `-1` to tell our Workflow to skip the approval
        * We could choose to be explicit and add `SkipApproval` to the input argument
            * This is the choice we make because it is clearer to maintainers
* Support an `Errors`  enumeration for raising a timed out OnboardEntity request.
