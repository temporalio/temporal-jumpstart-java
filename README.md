# jumpstart
Some repos Start. This one Jump Starts.

## Setup

// TODO implement script to bootstrap this when `make namespace` in conjunction with `mkcert`.

## Onboardings

You can run the `api` and `workers` Applications against your local Temporal dev server or
against Temporal Cloud. Just tune the `application-temporal-cloud.yaml` resources configuration as appropriate.


### Local

#### Run Temporal Server
``` $ temporal server start-dev ```

#### Run each Onboardings project separately

``` $ ./gradlew :onboardings:workers:bootRun ``` 

``` $ ./gradlew :onboardings:api:bootRun ```

### Temporal Cloud

#### Run each Onboardings project separately

``` $ ./gradlew :onboardings:workers:bootRun --args='--spring.profiles.active=temporal-cloud' ```

``` $ ./gradlew :onboardings:api:bootRun --args='--spring.profiles.active=temporal-cloud'```


### Troubleshooting

**`Error: Could not find or load main class org.gradle.wrapper.GradleWrapperMain`**

Run `gradle wrapper` to regen the missing code (we move fast).

