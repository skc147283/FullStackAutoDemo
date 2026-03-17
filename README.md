# Wealth API Demo (Interview Ready)

A Java Spring Boot REST API project with banking and wealth management use cases.

## Why this project is useful for interviews

This project demonstrates critical API engineering patterns used in fintech systems:
- Layered architecture: `controller -> service -> repository`
- Input validation and error contracts
- Idempotent transfers (`clientRequestId`) for payment safety
- Transaction history for auditability
- Portfolio rebalancing logic by risk profile
- Automated tests in both JUnit and TestNG
- Jenkins CI pipeline for build + test execution

## Tech stack

- Java 17
- Spring Boot 3
- Spring Web + Validation + Data JPA
- H2 in-memory DB
- OpenAPI/Swagger UI
- JUnit 5 + Spring MockMvc integration tests
- TestNG sample test
- Jenkins Pipeline

## API use cases implemented

1. Customer onboarding
2. Account opening with opening balance
3. Cash deposit into account
4. Fund transfer with idempotency key
5. Account statement by time window
6. Portfolio holdings update
7. Rebalance preview based on risk profile

## Run locally

```bash
mvn clean test
mvn spring-boot:run
```

Reusable demo checklist:
- `INTERVIEW_RUNBOOK.md`

Open:
- API docs: `http://localhost:8080/swagger-ui.html`
- H2 console: `http://localhost:8080/h2-console`

## Key endpoints

### Customer onboarding
`POST /api/v1/customers`

```json
{
  "fullName": "Asha Verma",
  "email": "asha.verma@example.com",
  "riskProfile": "BALANCED"
}
```

### Open account
`POST /api/v1/accounts`

```json
{
  "customerId": "<UUID>",
  "currency": "USD",
  "openingBalance": 1000.00
}
```

### Deposit
`POST /api/v1/accounts/{accountId}/deposit`

```json
{
  "amount": 100.00,
  "reason": "Monthly savings"
}
```

### Transfer (idempotent)
`POST /api/v1/accounts/transfer`

```json
{
  "sourceAccountId": "<UUID>",
  "destinationAccountId": "<UUID>",
  "amount": 250.00,
  "clientRequestId": "trf-2026-0001"
}
```

### Statement
`GET /api/v1/accounts/{accountId}/statement?from=2026-03-01T00:00:00Z&to=2026-03-31T23:59:59Z`

### Portfolio holding
`POST /api/v1/portfolios/{customerId}/holdings`

```json
{
  "symbol": "EQUITY",
  "marketValue": 12000.50
}
```

### Rebalance preview
`POST /api/v1/portfolios/{customerId}/rebalance-preview`

## Testing strategy

- `AccountApiIntegrationTest`: end-to-end API flow for customer/account/transfer/statement
- `AccountApiRestAssuredTest`: reusable REST Assured coverage for happy path and failure scenarios
- `PortfolioApiRestAssuredTest`: REST Assured coverage for holdings and rebalance flows
- `PortfolioServiceTestNg`: TestNG example for domain logic
- `WealthApiApplicationTests`: context boot sanity check
- `ARCHITECTURE_FLOW.md`: interview-ready controller -> service -> repository walkthrough

Run all tests:

```bash
mvn test
```

## API test package

Critical API workflow tests are grouped under:

- `src/test/java/com/interview/wealthapi/apitest`

This package contains end-to-end business flow validation using REST Assured.

## UI application for critical flow

A browser-based operations console is available at:

- `http://localhost:8080/index.html`

It covers the full critical flow:

1. Customer onboarding
2. Account opening
3. Deposit
4. Transfer
5. Portfolio holding
6. Rebalance preview

## UI automation: Selenium + BDD Cucumber + POM

UI automation uses:

- Selenium WebDriver 4
- Cucumber BDD on JUnit Platform
- Page Object Model classes under `src/test/java/com/interview/wealthapi/uitest/pages`

Run UI tests (visible browser by default on local machines, headless in CI):

```bash
mvn -Pui-tests verify
```

Optional runtime properties:

- `-Dui.base-url=http://localhost:8080`
- `-Dui.headless=true`

VS Code test runs default to the `Visible UI Browser` Java test configuration in `.vscode/settings.json`.
Switch to `Headless UI Browser` when you want local runs to stay off-screen.

Allure reporting is enabled for UI runs. Generate the HTML report after running UI tests with:

```bash
mvn -pl ui-tests allure:report
```

The raw results are written under `ui-tests/target/allure-results` and the generated report is created under `ui-tests/target/site/allure-maven-plugin`.

## Open-source database integration tests

PostgreSQL integration tests run with Testcontainers and are packaged under:

- `src/test/java/com/interview/wealthapi/dbtest`

Run DB tests:

```bash
mvn -Pdb-tests verify
```

## Jenkins CI/CD setup

1. Install Jenkins with JDK 17 and Maven 3 tools configured as:
  - `jdk17`
   - `maven3`
2. Create Pipeline job and point to this repo.
3. Jenkins reads `Jenkinsfile` and runs build + tests automatically.
4. Collect JUnit reports and archive JAR.

## Suggested open-source/cloud options for demo hosting

- **Render**: easy container deploy from GitHub
- **Railway**: fast Java app deployment for demos
- **Fly.io**: lightweight global app hosting
- **GitHub Actions + Docker Hub + Render/Fly**: good CI/CD story for interviews
- **Local Kubernetes with kind + Argo CD** (advanced demo)

## Deploy on Render (free tier friendly)

This repository already contains [render.yaml](render.yaml) and a Docker image build definition in [Dockerfile](Dockerfile).

### 1) Push code to GitHub

Render pulls from your repository, so make sure latest changes are pushed.

### 2) Create service from Blueprint

1. Go to Render dashboard.
2. Choose New -> Blueprint.
3. Select your repository.
4. Render will detect [render.yaml](render.yaml).
5. Create the service.

After deployment, Render gives you a URL like:

- https://wealth-api-demo.onrender.com

### 3) Verify deployed app

Open:

- Health: https://<your-render-url>/actuator/health
- Swagger: https://<your-render-url>/swagger-ui.html
- UI home: https://<your-render-url>/index.html
- UI dashboard: https://<your-render-url>/dashboard.html

### 4) Run UI E2E tests against deployed QA/SIT app

Run smoke + sanity in headless mode against deployed URL:

```bash
mvn -pl ui-tests verify \
  -Dit.test=SmokeUiIT,SanityUiIT \
  -Dui.headless=true \
  -Dui.base-url=https://<your-render-url>
```

Run only smoke for quick post-deploy validation:

```bash
mvn -pl ui-tests verify \
  -Dit.test=SmokeUiIT \
  -Dui.headless=true \
  -Dui.base-url=https://<your-render-url>
```

Generate Allure report for deployed run:

```bash
mvn -pl ui-tests allure:report
```

## Host with GitHub Actions + Docker Hub + Render/Fly

This repository now includes:

- GitHub Actions CI for tests: `.github/workflows/ci.yml`
- GitHub Actions Docker publish/deploy pipeline: `.github/workflows/docker-deploy.yml`
- GitHub Actions Render deploy + smoke E2E pipeline: `.github/workflows/render-smoke-e2e.yml`
- GitHub Actions Render UI smoke pipeline: `.github/workflows/render-ui-smoke.yml`
- Render service blueprint: `render.yaml`
- Fly app config: `fly.toml`

### 1) Create required accounts

- GitHub
- Docker Hub
- Render and/or Fly.io

### 2) Configure GitHub repository secrets

In GitHub repo settings -> Secrets and variables -> Actions, add:

- `DOCKERHUB_USERNAME`: your Docker Hub username
- `DOCKERHUB_TOKEN`: Docker Hub access token (not password)
- `RENDER_DEPLOY_HOOK_URL`: optional Render deploy hook URL
- `RENDER_BASE_URL`: deployed Render app base URL (for post-deploy smoke E2E), e.g. `https://wealth-api-demo.onrender.com`
- `FLY_API_TOKEN`: optional Fly personal access token

### 3) Docker image publish flow

On every push to `main` or `master`, workflow `docker-deploy.yml`:

1. Builds Docker image from `Dockerfile`
2. Pushes to `docker.io/<DOCKERHUB_USERNAME>/wealth-api-demo`
3. Tags image with branch/sha/latest (default branch)

### 4) Deploy to Render

Option A (recommended for demo):

1. In Render, create a new Web Service from Docker image.
2. Use image: `docker.io/<DOCKERHUB_USERNAME>/wealth-api-demo:latest`.
3. Add deploy hook URL into `RENDER_DEPLOY_HOOK_URL` secret.
4. Every successful Docker publish triggers Render deployment.

Option B:

1. Connect the repo directly in Render.
2. Render can also read `render.yaml` blueprint.

### 5) Deploy to Fly.io

1. Install and login once locally:

```bash
brew install flyctl
fly auth login
```

2. Create your Fly app name (must be globally unique), then update `fly.toml` `app` value.
3. Set `FLY_API_TOKEN` in GitHub secrets.
4. GitHub Actions auto-runs `flyctl deploy --remote-only` after Docker publish.

### 6) Verify hosted API

- Render URL example: `https://<your-render-service>.onrender.com/swagger-ui.html`
- Fly URL example: `https://<your-fly-app>.fly.dev/swagger-ui.html`

### 7) Verify hosted UI app

- Render URL example: `https://<your-render-service>.onrender.com/index.html`
- Render dashboard URL example: `https://<your-render-service>.onrender.com/dashboard.html`

The workflow `.github/workflows/render-ui-smoke.yml` can be run manually or after Docker publish to verify these endpoints.

## Free Jenkins + CI/CD setup

If you want Jenkins with zero hosting cost, run it locally using Docker Compose:

```bash
docker compose -f docker-compose.jenkins.yml up -d
```

Then open `http://localhost:8081` and complete setup.

Recommended Jenkins setup for this repo:

1. Install suggested plugins: Pipeline, Git, Maven Integration, JUnit.
2. Configure tools in Jenkins Global Tool Configuration:
  - JDK name: `jdk17`
  - Maven name: `maven3`
3. Create a Pipeline job from this repository.
4. Use `Jenkinsfile` in repo root.

Free CI/CD recommendation for interview demos:

1. Keep GitHub Actions as primary cloud CI/CD (free minutes).
2. Use local Jenkins only for showcasing classic Jenkins pipeline execution.
3. Trigger Render deploy from GitHub Actions and verify UI/API with smoke workflows.

Use these links in interview demos to show cloud-hosted APIs and CI/CD automation.

## Interview talking points

- Explain why idempotency is mandatory for transfer APIs.
- Describe how API tests protect against regressions in payment flows.
- Show CI pipeline quality gates for every pull request.
- Discuss next steps for production: auth (OAuth2), rate limiting, observability, and fraud checks.
