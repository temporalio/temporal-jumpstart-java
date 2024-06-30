# Starters

## Goals

* Understand how to integrate `Temporal Client` into an API
* Understand `WorkflowStartOptions` enough to make the right choice for your Use Case
* Understand how to Start OR Execute a Workflow (that doesn't exist!)
* Introduce Web UI and a Workflow Execution history

## Onboardings

## Connection setup

See the various `resources` directories for connection details using Spring Boot.

## Start an Entity Onboarding

1. Run all services (see main README)
2. Issue a request with the Swagger UI for `V1` paths that opens up OR
    1. Using your favorite HTTP Client send `PUT` request like
        1. `http PUT http://{HOSTNAME}/api/v1/onboardings/onboarding-123 value=some-value`
3. Now issue a `GET` to the same resource to see the input parameters and execution status of your Workflow
    1. eg `http GET http://{HOSTNAME}/api/v1/onboardings/onboarding-123`

**Expected outcome**

1. You should see a Workflow running  [here](http://localhost:8233/namespaces/default/workflows) or  [Temporal Cloud Namespace UI](https://cloud.temporal.io).
    1. WorkflowType: `WorkflowDefinitionDoesntExistYet`
    2. WorkflowId: `onboarding-123`
2. Enter the Workflow and you should see a "No Workers Running" caution and the first two events which indicate the
   Workflow has been scheduled to execute.
