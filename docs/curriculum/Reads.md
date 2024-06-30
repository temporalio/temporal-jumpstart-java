# Reads

Temporal exposes two primary facilities for reading Workflow state, `Query` and `Search Attribute`.

These two are very different in what they are for, how they work, how they are secured, and in what part of your
Application you should use them.

## Search Attributes

Custom Search Attribute values, backed by a Visibility storage which is separate from execution history storage,
are assigned by an Execution to be used as search criteria across _n_ Workflows.
This is useful for many support and operational concerns.
While this would also be useful in business feature searches as well, we recommend you avoid using them for
customer-facing search experiences.

This is primarily due to the _eventual consistency_ guarantee these values have.
For example, a value for "CustomerBalance" might be assigned in the Workflow Execution but would not immediately
reflect changes to it within the Signal that caused its change.

Note too that keys and values in the Visibility storage are not passed through [Data Converter](https://docs.temporal.io/dataconversion#custom-data-converter).
This is important when considering how to secure your data.

## Query

Customer-facing experiences often leverage the `Query` to reveal
consistent Workflow Execution state. Strongly-typed and convenient, these queries
compute a value based on current history on demand in response to a special Query task from a Temporal Client.

That means you need Workers running to process Queries.

It also means you are using your Workers to read state the same as other Tasks that perform business operations.


### Gotchas
Temporal employs alot of optimizations to make Query a typically fast, easy, and reliable feature but
there are some things to keep in mind when reaching for `Query` in a customer experience.

* They take up a cache entry on a Worker
* They can get into races with the Continue-As-New
* They are inherently subject to some latency caused by using a Message Broker to return a value.

#### Polling
Polling for a value with a Workflow Query can be done, but if the same execution
is expected to serve that Query to a large volume of callers note expect a bottleneck to form
on that Worker host serving the Query. Tight-looped, short-frequency polling can lead to the same effect.

#### Many Long-Running Executions
Imagine that a Workflow Type you have built runs forever and are co-located on the same
Task Queue as other transactional Workflow Types. They do not perform work very frequently.

Now you need to ship a feature thet needs to Query these long-lived Workflows that are over 60 days old.

When you first ship the feature only a small group of Workflow executions being Queried will take up
execution [slots](https://docs.temporal.io/develop/worker-performance#worker-executor-slots-sizing) used
to perform Task execution. As your adoption increases, however, those Workflows that are long-lived will accumulate
and take up more and more room to serve the Queries.
Ultimately this can lead to a requirement to keep more capacity on-hand to serve Queries for these prospective Workflows.

#### Lifetime
Unlike other persistence storage, the *Query* will only return results for a Workflow that is *Open* or is
*Closed* and has not been purged due to the Namespace _retention period_ specification.
If you need longer access to the Workflow's data, reach for a proper storage.

## Refactoring Onboarding Use Case

We need to support the View of our submitted Entity Onboarding by fetching the Workflow
state that represents it.

We will use *Query* to retrieve this state inside our `GET /onboardings/{id}` API handler.

* Introduce explicit `queries` message to meet our *GetEntityOnboardingState* request
* Remove the complex history collection in our `GET` handler and reduce the code to use our *GetEntityOnboardingStatus* Query.
* Change our `GET` result to reflect our UI that needs
    * Approval status and Comment
    * Current Value
    * Id