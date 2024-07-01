# Activities

* Introduce our first Activity test using `ActivityTestEnvironment`
* Discuss the importance of _idempotency_ 
* Understand Dependency Injection in our Activity implementations
* Understand `ActivityOptions` 
  * Discuss the different `Timeouts` available 
  * Understand `RetryOptions` and gotchas
  * Understand choosing `NonRetryable` failure type ownership

## Onboardings

// TODO fix references from dotNET SDK packages to our Java modules here

## Workflow : Introduce our first Activity

Our requirements specify that we need to register this Entity with our CRM System.
Calls to APIs, Databases, filesystems, etc should always be done inside an `Activity`.

Let's continue doing top-down development by first Executing this activity in our `OnboardEntity` Workflow.

1. Add the "RegisterCrmEntity" test in our Workflow unit test, asserting that the activity was invoked correctly.
2. See it fail in tests.
3. Update the `OnboardEntity` Workflow to invoke the `RegisterCrmEntity` activity.
4. See it succeed in the tests.

### TaskQueue assignment

Temporal will schedule Activity tasks on the same TaskQueue as the Workflow that executes it by default.
Task Queue assignment, done through Activity Options, is useful especially when:

* You need to target a specific host because of the resources the Activity needs or due to cost.
* The underlying behavior in the Activity requires some kind of rate-limiting.
* Multi-tenancy segregation is required in your deployment scheme; for example, giving some tenants higher priority than others due to business classification.
* You want to "intercept" work to run in other environments dynamically.

## Activity: Implement our first Activity

Let's introduce the `RegisterCrmEntity` Activity implementation.
There are a few refactorings we will perform once we write our test that proves our behavior.

One of the main things we need to ensure in our Activity implementations is _idempotency_.
Here, we want to verify the `Entity` does not already exist in the CRM before attempting to register it.

### Refactorings

- `Messages.Commands`
- `Domain.Handlers`
- `Domain.Clients.ICrmClient`

_Test-Drive our `RegisterCrmEntity` Activity`_

Let's use our first Activity test to drive out all the third-party Clients, Integrations packages,
and establish the message contracts for our first Activity.
We will create Namespaces for each of our third-party client dependencies we plan on using in our Activity Handlers.

_Introduce `Messages.Commands`_

Our `Commands` package will hold the contracts we use to Request operations with our Activity Handlers.

_Introduce `Domain.Handlers.Integrations`_

Our `Activities` are the atomic operations, the "steps", we will use to get things done in our Use Case.
We will group the calls to our third party dependencies (eg our "CRM" software) into an explicit
namespace called "Integrations" and create the message contracts our Orchestration can use to execute
these Activity Handlers.

_Introduce `CrmClient` to `Domain.Clients`_

The CRM Client needs to be created at Application startup and injected into our Activity Handlers.
This client requires an "API Key" to be passed into it upon creation.

## RetryOptions

Now that we understand the CRM Client integration, we need to think about how we should configure
the RetryOptions for this Activity.

Should we just let the default retry policy of "try forever" happen?
Or should we instead only try registering the Entity for a period of time before doing something else?
Maybe we want to keep retrying but gradually backoff to ease pressure on our CRM service?

### NonRetryable Errors

You can give Temporal hints about which `ErrorType` should not cause a Retry when invoking an Activity.
There are two "sides" to configure these (string) `ErrorType` exclusions.
Which you do, depends on who "owns" the retry rule.

1. **_Workflow Owner_** : When calling the activity *from the Workflow* you can provide the `ErrorType` as one of the `NonRetryableErrorTypes`
  1. This is useful when the Workflow wants to enforce some kind of compensation logic or short-circuiting within a time span.
  2. Avoid coupling Workflow execution path to low-level exceptions that should be more generalized in your Activities.
2. **_Activity Owner_** : When an Activity raises an `ApplicationFailureException` and sets the `nonRetryable` flag to `true`
  1. This is useful if your Activity knows it will never recover and so will cause a stuck Workflow.
  2. A Workflow author should often not be coupled to such low-level concerns so the Activity can own this rule.
  3. Avoid coupling Activity retryability to some assumption about how it is being executed. Setting this flag from the Activity should be reserved for cases where it will never succeed.
