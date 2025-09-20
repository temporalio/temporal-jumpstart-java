# Temporal Jumpstart App

This directory contains the main Temporal application components:

- **api**: Spring Boot REST API server (port 3030)
- **workers**: Temporal workflow workers
- **domain**: Shared domain models and workflows

## Prerequisites

- Java 17+
- Temporal Server (local dev server or Temporal Cloud)

## Running the Applications

### Local Development (with Temporal dev server)

1. **Start Temporal Server**
   ```bash
   temporal server start-dev
   ```

2. **Run the Workers** (from project root)
   ```bash
   ./gradlew :app:workers:bootRun
   ```

3. **Run the API Server** (from project root)
   ```bash
   ./gradlew :app:api:bootRun
   ```

The API server will be available at `http://localhost:3030`

### Temporal Cloud

1. **Run the Workers**
   ```bash
   ./gradlew :app:workers:bootRun --args='--spring.profiles.active=temporal-cloud'
   ```

2. **Run the API Server**
   ```bash
   ./gradlew :app:api:bootRun --args='--spring.profiles.active=temporal-cloud'
   ```

## Configuration

- **API**: Configuration in `api/src/main/resources/application.yaml`
- **Workers**: Configuration in `workers/src/main/resources/application.yaml`

The workers are configured to:
- Connect to Temporal server at `127.0.0.1:7233` (local)
- Use task queue: `apps`
- Run workflow: `MyWorkflowImpl`
- Execute activities from bean: `my-activities`

## Monitoring

Both applications expose Prometheus metrics at `/actuator/prometheus` endpoint.