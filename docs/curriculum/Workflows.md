# Workflows

### Goals

* Introduce `/tests` considerations
* Introduce our first test using the `TestWorklowEnvironment`
* Understand `Failure` versus `Exception` errors, their impact on Workflow executions, and how to work with them

## Onboardings

### Refactorings

- `Messages`
- `Api`
- `Domain`

_Extract Messages_

Introduce an explicit `Messages` project that establishes the contracts between our services.

_Introduce `Domain`_

Encapsulate our business rules inside a `Domain` project. This is where our Orchestration Workflows and
related business rules will be maintained.

_Rename `Starters` to `Api`_

Explicitly expose our REST API surface and make changes to use strongly typed Workflows in our Starter code.
Note that we can take a reference directly onto our `Domain` project right now for the Workflow Definitions.

_Move `Clients` to `Domain`_

Use the `Domain` package to expose reusable components for the `Api`, including the Temporal Client
and respective Config. We might extract that later for reuse across other Applications, but this is enough for now.