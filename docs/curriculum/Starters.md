# Starters

## Goals

* Understand how to integrate `Temporal Client` into an API
* Understand `WorkflowStartOptions` enough to make the right choice for your Use Case
* Understand how to Start OR Execute a Workflow (that doesn't exist!)
* Introduce Web UI and a Workflow Execution history

## Best Practices

#### WorkflowIds should have business meaning.

This identifier can be an AccountID, SessionID, etc. 

* Prefer _pushing_ an WorkflowID down instead of retrieving after-the-fact. 
* Acquaint your self with the "Workflow ID Reuse Policy" to fit your use case
Reference: https://docs.temporal.io/workflows#workflow-id-reuse-policy

#### Do not fail a workflow on intermittent (eg bug) errors; prefer handling failures at the Activity level within the Workflow.

A Workflow will very rarely need one to specify a RetryPolicy when starting a Workflow and we strongly discourage it.
Only Exceptions that inherit from `TemporalFailure` will cause a RetryPolicy to be enforced. Other Exceptions will cause the WorkflowTask
to be rescheduled so that Workflows can continue to make progress once repaired/redeployed with corrections.
Reference: 
* https://javadoc.io/doc/io.temporal/temporal-sdk/latest/io/temporal/client/WorkflowFailedException.html
* https://docs.temporal.io/encyclopedia/detecting-workflow-failures


## Onboardings

#### Select Our Options

* **Workflow ID:** Let's use the same `id` as the resource that has been `PUT`
* **Workflow ID Reuse Policy:** Our requirements state that we want to allow the same WorkflowID if prior attempts were Canceled. 
Therefore, we are using this Policy that will reject duplicates unless previous attempts did not reach terminal state as `Completed'.

## Start an Entity Onboarding

1. Run all services (see main README)
2. Issue a request with the Swagger UI for `V1` paths that opens up OR
    1. Using your favorite HTTP Client send `PUT` request like
        1. `http PUT http://{HOSTNAME}/api/v1/onboardings/onboarding-123 value=some-value`
3. Now issue a `GET` to the same resource to see the input parameters and execution status of your Workflow
    1. eg `http GET http://{HOSTNAME}/api/v1/onboardings/onboarding-123`

**Expected outcome**

1. You should see a Workflow running  [locally](http://localhost:8233/namespaces/default/workflows) or in the [Temporal Cloud Namespace UI](https://cloud.temporal.io).
    1. WorkflowType: `WorkflowDefinitionDoesntExistYet`
    2. WorkflowId: `onboarding-123`
2. Enter the Workflow and you should see a "No Workers Running" caution and the first two events which indicate the
   Workflow has been scheduled to execute.
