# Workers

## Goals

- Introduce the Temporal Worker
- Review Task Fundamentals
- Understand high level about Executors and Pollers to guide Worker configuration and deployment
- Run the Worker with our Workflow and Activity registered

## Task Fundamentals

![Task fundamentals](task.png)

## Worker Fundamentals

![Worker fundamentals](worker.png)

## Try It Out

### Swagger (Browser) Client

Ensuring you are using either `cloud` or `local` launchSettings for both services:

1. Run the `Api` Program.
2. Run the `Services` Program .
3. Visit the Swagger UI and
    1. `PUT` a `/onboardings/{id}` request.
        1. Note that if you include `timeout` text in the `value` you can simulate a Workflow failure when trying to "RegisterCrmEntity"
    2. `GET` a `/onboardings/{id}` request (using the ID you provided)
        1. See the response includes the Workflow Status
4. Visit the Temporal Web UI and verify that the Workflow has Completed or Failed as expected.

### Command Line / Temporal CLI

From the project root -

#### *Local*

In a terminal:
```shell
dotnet run --launch-profile local \
 --project src/Temporal.Curriculum.Workers/Temporal.Curriculum.Workers.Services/Temporal.Curriculum.Workers.Services.csproj
```

In another terminal:
```shell
temporal workflow start \
  --workflow-id onboarding-workers-999 \
  --task-queue onboardings \
  --type OnboardEntity \
  --input '{"Id": "onboarding-workers-999", "Value": "helloworkers"}' # pay attention to JSON case
```

#### *Cloud*

In a terminal:
```shell
dotnet run --launch-profile cloud \
 --project src/Temporal.Curriculum.Workers/Temporal.Curriculum.Workers.Services/Temporal.Curriculum.Workers.Services.csproj
```

In another terminal:
```shell
export ACCOUNT="sdvdw"
export NAMESPACE="temporal-jumpstart-dotnet.$ACCOUNT"
export ADDRESS="$NAMESPACE.tmprl.cloud:7233"
export TLS_CERT_PATH="localhost-client.pem"
export TLS_KEY_PATH="localhost-client-key.pem"

temporal workflow start \
  --workflow-id onboarding-workers-999 \
  --task-queue onboardings \
  --type OnboardEntity \
  --input '{"Id": "onboarding-workers-999", "Value": "hellocloudworkers"}' \
  --address "$ADDRESS" \
  --namespace "$NAMESPACE" \
  --tls-cert-path $TLS_CERT_PATH \
  --tls-key-path $TLS_KEY_PATH
```