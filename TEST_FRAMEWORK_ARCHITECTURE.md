# Test Framework Architecture Guide

## Table of Contents
1. [Overview](#overview)
2. [Architecture Diagram](#architecture-diagram)
3. [Folder Structure](#folder-structure)
4. [Module Organization](#module-organization)
5. [Test Execution Guide](#test-execution-guide)
6. [Technology Stack](#technology-stack)
7. [Design Patterns & Decisions](#design-patterns--decisions)
8. [Key Components](#key-components)
9. [Interview Talking Points](#interview-talking-points)

---

## Overview

This is a **three-tier test automation framework** built on Maven with Spring Boot, designed to validate a wealth management API and UI across multiple layers:
- **Unit Tests**: Business logic validation (Surefire)
- **Integration Tests**: Database operations with Testcontainers (Failsafe)
- **UI Tests**: End-to-end Selenium + BDD (Failsafe with Cucumber & TestNG)

**Key Philosophy**: *Maximize local developer productivity (visible browser) while enabling CI headless execution and data-driven test coverage.*

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│               Maven Multi-Module Project                     │
│                   (Parent POM)                               │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │   api/       │  │  ui-tests/   │  │  db-tests/   │       │
│  │ (Spring App  │  │  (Selenium   │  │(Testcontai-  │       │
│  │  + API tests)│  │   + Cucumber │  │  ners ITs)   │       │
│  │              │  │   + TestNG)  │  │              │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│        ▲                  ▲                   ▲               │
│        │                  │                   │               │
│   Surefire          Failsafe (Dual)      Failsafe            │
│   Unit Tests        Execution:           DB Tests            │
│                     - Cucumber           (ITs)               │
│                     - TestNG (Data-                          │
│                       Driven)                                │
│                                                               │
└─────────────────────────────────────────────────────────────┘

EXECUTION FLOW:
Maven Verify
    ├── Surefire (all *Test.java)
    │   └── api/src/test → Unit tests @ integration-test phase
    ├── Failsafe (all *IT.java)
    │   ├── api/ → API controller/integration tests
    │   ├── ui-tests/ → Dual Execution:
    │   │   ├── cucumber-ui: CriticalUiIT (@critical journey)
    │   │   └── testng-ui: CriticalBusinessUiNgIT (9 data-driven cases)
    │   └── db-tests/ → CustomerRepositoryPostgresDbIT (Testcontainers)
    └── Reports Generated
        ├── target/surefire-reports/ (Surefire HTML)
        ├── target/failsafe-reports/ (Failsafe XML)
        └── target/allure-results/ (Allure JSON)
```

---

## Folder Structure

```
RestAPI_UI_DB/
│
├── pom.xml                              # Parent POM (properties, dependencies, plugins)
├── README.md                            # Project overview
├── ARCHITECTURE_FLOW.md                 # Data flow & system design
├── INTERVIEW_RUNBOOK.md                 # Quick reference for interviews
│
├── api/                                 # Java Spring Boot Application
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/interview/wealthapi/
│   │   │   │   ├── WealthApiApplication.java        # Startup
│   │   │   │   ├── controller/                      # REST endpoints
│   │   │   │   ├── service/                         # Business logic
│   │   │   │   ├── repository/                      # DB access (JPA)
│   │   │   │   └── dto/                             # Data transfer objects
│   │   │   └── resources/
│   │   │       ├── application.yml                  # Config
│   │   │       └── static/index.html                # Simple UI
│   │   │
│   │   └── test/
│   │       ├── java/com/interview/wealthapi/
│   │       │   ├── AccountApiIntegrationTest.java   # API route tests
│   │       │   ├── AccountApiRestAssuredTest.java   # RESTAssured API tests
│   │       │   ├── PortfolioApiRestAssuredTest.java # Portfolio endpoint tests
│   │       │   ├── CriticalBusinessFlowApiTest.java # Business rule API tests
│   │       │   └── WealthApiApplicationTests.java   # Spring context test
│   │       └── resources/
│   │           └── test-data.sql                    # Test fixtures
│   │
│   └── target/
│       ├── classes/                    # Compiled main code
│       └── test-classes/               # Compiled tests
│
├── ui-tests/                            # UI Test Module (Selenium + BDD)
│   ├── pom.xml
│   └── src/test/
│       ├── java/com/interview/wealthapi/uitest/
│       │   ├── support/
│       │   │   ├── WebDriverFactory.java            # Singleton WebDriver mgmt
│       │   │   ├── UiTestRuntime.java               # Spring app bootstrap
│       │   │   ├── WealthDashboardPage.java         # Page Object Model (POM)
│       │   │   └── DriverContextHolder.java         # ThreadLocal driver context
│       │   │
│       │   ├── critical/
│       │   │   ├── CriticalUiIT.java                # Cucumber journey test (@critical tag)
│       │   │   └── CriticalBusinessUiNgIT.java      # TestNG data-driven test (9 scenarios)
│       │   │
│       │   └── hooks/
│       │       └── UiHooks.java                     # Cucumber lifecycle (Before/After)
│       │
│       └── resources/
│           ├── features/
│           │   └── critical_business_flow.feature   # BDD scenario definition
│           └── cucumber.properties                  # Cucumber config
│
├── db-tests/                            # Database Test Module (Testcontainers)
│   ├── pom.xml
│   └── src/test/
│       ├── java/com/interview/wealthapi/dbtest/
│       │   └── CustomerRepositoryPostgresDbIT.java  # DB integration tests
│       └── resources/
│           └── schema-test.sql                      # Test schema setup
│
└── target/
    ├── surefire-reports/                # Unit test reports
    ├── failsafe-reports/                # Integration test reports
    ├── cucumber-reports/                # Cucumber HTML reports
    └── allure-results/                  # Allure report data
```

---

## Module Organization

### 1. **api/** - Spring Boot Application + API Tests

**Purpose**: Core wealth management service and its API layer validation

**Key Files**:
- `WealthApiApplication.java` - Spring Boot entry point, starts on random port in tests
- `application.yml` - Configuration (H2 in-memory DB for tests)
- `*IT.java` - API integration tests (AccountApiIntegrationTest, PortfolioApiRestAssuredTest, etc.)

**Test Discovery**: `*Test.java` (Surefire) + `*IT.java` (Failsafe)

**Example API Test**:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountApiRestAssuredTest {
    
    @Test
    void shouldCreateAccountAndValidateBalance() {
        // REST Assured API testing directly against endpoints
        given()
            .contentType(ContentType.JSON)
            .body(accountRequest)
        .when()
            .post("/api/accounts")
        .then()
            .statusCode(201)
            .body("balance", is(1000.0));
    }
}
```

---

### 2. **ui-tests/** - Selenium + BDD + Data-Driven Tests

**Purpose**: End-to-end UI validation through browser automation

**Architecture Layers**:

#### a) **Support Layer** (Helper & Infrastructure)
- `WebDriverFactory.java` - Singleton WebDriver management
  - Auto-detects headless mode via `ui.headless` property or CI env
  - Configures ChromeOptions (headless, no-sandbox, disable-gpu)
  - Thread-safe via ThreadLocal

- `UiTestRuntime.java` - Spring app lifecycle management
  - Starts WealthApiApplication on random port before test suite
  - Injects `ui.base-url` system property (e.g., http://localhost:12345)
  - Used by both Cucumber hooks and TestNG @BeforeClass

- `WealthDashboardPage.java` - Page Object Model (POM)
  - Encapsulates UI selectors and interactions
  - Methods like `depositFunds()`, `transferMoney()`, `rebalancePortfolio()`
  - Returns page state or domain objects (portfolio details)

#### b) **Test Types**

**Cucumber (BDD Journey Testing)**
- File: `CriticalUiIT.java` (Cucumber JUnit Platform Suite runner)
- Feature: `critical_business_flow.feature` (Gherkin syntax)
- Executes via: `cucumber-ui` Failsafe execution (excludes *UiNgIT)
- Single comprehensive scenario: Customer onboarding → deposit → transfer → rebalance
- **Use Case**: Demonstrate business flow understanding, storytelling in interviews

**TestNG (Data-Driven Critical Cases)**
- File: `CriticalBusinessUiNgIT.java` (JUnit Platform TestNG engine)
- DataProvider methods return 9 test cases across 3 categories:
  1. **Rebalance Validation** (3 cases): Conservative, Balanced, Aggressive target allocations
  2. **Idempotent Transfers** (2 cases): Duplicate request handling
  3. **Negative Rules** (4 cases): Negative amount, zero deposit, insufficient balance, same-account transfer
- **Use Case**: Demonstrate test parameterization, edge case coverage, complex assertion logic

#### c) **Execution Strategy**

```
Maven Failsafe (ui-tests module):
  │
  ├─ cucumber-ui execution
  │  └─ Includes: *UiIT (Cucumber suite runner)
  │     Excludes: *UiNgIT
  │
  └─ testng-ui execution
     Includes: *UiNgIT (TestNG data-driven)
     Excludes: *UiIT
```

This dual execution allows:
- Separate execution pools (parallel possible)
- Clear reporting for each test type
- Ability to run just Cucumber or just TestNG

---

### 3. **db-tests/** - Database Integration Tests

**Purpose**: Validate database operations and repository layer

**Technology**: Testcontainers PostgreSQL container

**Key File**:
- `CustomerRepositoryPostgresDbIT.java` - JPA repository tests against real PostgreSQL

**Example**:
```java
@Testcontainers
@DataJpaTest
@Import(TestConfiguration.class)
public class CustomerRepositoryPostgresDbIT {
    
    @Container
    static PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse(...));
    
    @Test
    void shouldSaveAndRetrieveCustomer() {
        Customer customer = new Customer("John", "Doe");
        Customer saved = customerRepository.save(customer);
        
        assertThat(customerRepository.findById(saved.getId()))
            .isPresent()
            .hasFieldOrPropertyWithValue("firstName", "John");
    }
}
```

---

## Test Execution Guide

### Quick Reference Commands

#### 1. **Run All Tests (Unit + Integration + UI)**
```bash
mvn verify
```
- Executes Surefire (unit tests) → Failsafe (integration + UI tests)
- Browser: **Visible** (default ui.headless=false)
- Time: ~2-3 minutes
- Reports: `target/surefire-reports/` + `target/failsafe-reports/` + `target/cucumber-reports/`

#### 2. **Run Only Unit Tests (Fast Feedback)**
```bash
mvn test
```
- Executes only Surefire phase
- No integration tests or UI tests
- Time: ~10-15 seconds
- **Best for**: Local development, quick CI validation

#### 3. **Run Only UI Tests (Local Dev with Visible Browser)**
```bash
mvn -pl ui-tests -am verify
```
- Runs ui-tests module with api dependency
- Browser: **Visible Chrome** (opening window on desktop)
- Executes: 1 Cucumber journey + 9 TestNG data-driven cases = **10 tests**
- Time: ~2 minutes
- **Best for**: Developer local validation, seeing test execution

#### 4. **Run Only Cucumber BDD Tests**
```bash
mvn -pl ui-tests -am -Dit.test=CriticalUiIT failsafe:integration-test failsafe:verify
```
- Runs single comprehensive journey scenario
- Time: ~30 seconds
- **Best for**: Journey/happy-path validation, story clarity

#### 5. **Run Only TestNG Data-Driven Tests**
```bash
mvn -pl ui-tests -am -Dit.test=CriticalBusinessUiNgIT failsafe:integration-test failsafe:verify
```
- Runs 9 parameterized cases (3 rebalance + 2 idempotent + 4 negative)
- Time: ~90 seconds
- **Best for**: Edge case coverage, regression testing

#### 6. **Run UI Tests in Headless Mode (CI-Style)**
```bash
mvn -pl ui-tests -am -Dui.headless=true verify
```
- Browser: **Headless Chrome** (no GUI window)
- All tests run in background, faster, CI-friendly
- Time: ~1.5 minutes
- **Best for**: CI/CD pipelines, integration server runs

#### 7. **Run Specific 3 Critical Cases (Rebalance Allocations)**
```bash
mvn -pl ui-tests -am -Dui.headless=true \
  -Dit.test=CriticalBusinessUiNgIT verify
```
- Runs first 3 TestNG DataProvider cases (Conservative, Balanced, Aggressive)
- Headless mode, ~45 seconds
- **Best for**: Quick validation before commit

#### 8. **Run Only Database Tests**
```bash
mvn -pl db-tests -am verify
```
- Tests CustomerRepository against real PostgreSQL container
- Time: ~1 minute (includes container startup)
- **Best for**: Database layer validation

#### 9. **View Reports**

**Available Reports After Test Execution:**

```bash
# Cucumber BDD Report (Recommended for Journey Tests)
open target/cucumber-reports/ui-critical.html

# Surefire Unit Test Report (XML format - requires parsing or Maven plugin)
open target/surefire-reports/index.html

# Failsafe Integration Test Report (XML format - requires parsing or Maven plugin)
ls target/failsafe-reports/*.xml
```

**Allure Report (Optional - Enhanced Reporting)**

Allure provides interactive dashboards with timelines, history, and detailed metrics. To generate an Allure report:

*Option 1: Using Allure CLI (Recommended)*
```bash
# Prerequisites: brew install allure (if not already installed)
cd /Users/sureshkc/Desktop/Interview/API/RestAPI_UI_DB

# Run tests with Allure listeners enabled
mvn -pl ui-tests -am verify

# Generate Allure report from results
allure generate target/allure-results -o target/allure-report --clean

# View report
open target/allure-report/index.html
```

*Option 2: Using Maven Plugin (if properly configured)*
```bash
mvn allure:report
open target/site/allure-report/index.html
```

**Note**: Allure integration requires:
- `allure-cucumber7-jvm` and `allure-testng` artifacts in dependencies (✅ Already configured)
- Allure listeners to be auto-discovered during test execution
- `allure-results/` directory to be populated with JSON result files
- Either Allure CLI or Maven plugin for HTML report generation

### Property Modifiers

| Property | Value | Impact |
|----------|-------|--------|
| `ui.headless` | `false` (default) | Visible Chrome browser opens |
| `ui.headless` | `true` | Headless Chrome runs in background |
| `CI` | environment set | Auto-enables headless mode |
| `skipITs` | `true` | Skips Failsafe entirely |
| `maven.test.skip` | `true` | Skips all tests (Surefire + Failsafe) |

### Environment Variables

```bash
# CI environment triggers headless mode automatically
export CI=true
mvn verify  # Will run with -Dui.headless=true

# Override local settings
export ui.headless=true
mvn -pl ui-tests -am verify
```

---

## Technology Stack

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| **Build** | Maven | 3.x | Multi-module project management |
| **Language** | Java | 17+ | Source code |
| **Framework** | Spring Boot | 3.4.3 | Web application server |
| **Database** | H2 (mem) / PostgreSQL | Latest | Test data persistence |
| **API Tests** | REST Assured | 5.x | HTTP API assertion |
| **UI Tests** | Selenium WebDriver | 4.27.0 | Browser automation |
| **WebDriver** | WebDriverManager | 5.x | Automatic ChromeDriver management |
| **BDD** | Cucumber | 7.20.1 | Behavior-driven development scenarios |
| **Data-Driven** | TestNG | 7.10.2 | Parameterized test execution |
| **Containers** | Testcontainers | Latest | Ephemeral Docker containers for DB tests |
| **Reporting** | Allure | 2.x | Enhanced test report visualization |

---

## Design Patterns & Decisions

### 1. **Singleton WebDriver (Thread-Safe)**

**Problem**: Multiple test threads could create conflicting WebDriver instances.

**Solution**: ThreadLocal pattern with synchronized lazy initialization in `WebDriverFactory`.

```java
public class WebDriverFactory {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    
    public static synchronized WebDriver getOrCreate() {
        if (driver.get() == null) {
            // Create and store in ThreadLocal
            driver.set(createWebDriver());
        }
        return driver.get();
    }
    
    public static void quit() {
        WebDriver d = driver.get();
        if (d != null) {
            d.quit();
            driver.remove();
        }
    }
}
```

**Interview Angle**: Thread safety in test automation, avoiding shared state.

---

### 2. **Page Object Model (POM)**

**Problem**: UI selectors hardcoded in test logic leads to brittle tests.

**Solution**: Encapsulate UI elements and interactions in `WealthDashboardPage.java`.

```java
public class WealthDashboardPage {
    private static final By DEPOSIT_INPUT = By.id("depositAmount");
    private static final By SUBMIT_BUTTON = By.xpath("//button[text()='Submit']");
    
    public void depositFunds(String amount) {
        driver.findElement(DEPOSIT_INPUT).sendKeys(amount);
        driver.findElement(SUBMIT_BUTTON).click();
        waitForLoadingToComplete();
    }
}
```

**Benefits**:
- Single source of truth for selectors
- Reusable across tests
- Easy maintenance when UI changes
- Readable test code

---

### 3. **Shared Test Runtime (Spring App Bootstrap)**

**Problem**: Each UI test needs the Spring app running, but startup is expensive.

**Solution**: `UiTestRuntime.java` starts app once per test suite, shared across all tests via static initialization.

```java
public class UiTestRuntime {
    private static ConfigurableApplicationContext appContext;
    
    public static synchronized void ensureAppStarted() {
        if (appContext == null) {
            appContext = SpringApplication.run(WealthApiApplication.class, 
                "--server.port=0");  // Random port
            // Inject port as system property
            System.setProperty("ui.base-url", 
                "http://localhost:" + getPort(appContext));
        }
    }
}
```

**Interview Angle**: Test performance optimization, lifecycle management.

---

### 4. **Dual Test Execution (Cucumber + TestNG in Same Module)**

**Problem**: Want both BDD journey tests and data-driven case validation in one module.

**Solution**: Two separate Failsafe executions with include/exclude patterns.

**Parent pom.xml - Properties**:
```xml
<properties>
    <cucumber.version>7.20.1</cucumber.version>
    <testng.version>7.10.2</testng.version>
</properties>
```

**ui-tests/pom.xml - Dual Failsafe Config**:
```xml
<execution>
    <id>cucumber-ui</id>
    <goals><goal>integration-test</goal></goals>
    <phase>integration-test</phase>
    <configuration>
        <includes>
            <include>**/CriticalUiIT.java</include>
        </includes>
        <excludes>
            <exclude>**/*UiNgIT.java</exclude>
        </excludes>
    </configuration>
</execution>

<execution>
    <id>testng-ui</id>
    <goals><goal>integration-test</goal></goals>
    <phase>integration-test</phase>
    <configuration>
        <includes>
            <include>**/*UiNgIT.java</include>
        </includes>
        <excludes>
            <exclude>**/CriticalUiIT.java</exclude>
        </excludes>
    </configuration>
</execution>
```

**Interview Angle**: Maven plugin lifecycle customization, modular test organization.

---

### 5. **Headless Mode Toggle via System Property**

**Problem**: Local dev wants visible browser, CI wants headless for speed & no GUI overhead.

**Solution**: `ui.headless` Maven property flows through to WebDriverFactory.

**Maven Property** (pom.xml):
```xml
<ui.headless>false</ui.headless>  <!-- default visible -->
```

**Failsafe Configuration**:
```xml
<systemPropertyVariables>
    <ui.headless>${ui.headless}</ui.headless>
</systemPropertyVariables>
```

**WebDriver Factory Check**:
```java
private static boolean isHeadlessEnabled() {
    return Boolean.parseBoolean(System.getProperty("ui.headless", "false"))
        || System.getenv("CI") != null;
}
```

**CLI Override**:
```bash
mvn verify -Dui.headless=true  # Force headless
```

**Interview Angle**: Environment-aware testing, CI/local dev differentiation.

---

### 6. **Data-Driven via TestNG DataProvider**

**Problem**: Want to test same business logic with multiple input combinations (positive + negative cases).

**Solution**: TestNG `@DataProvider` annotation with multiple `@Test` methods.

```java
@DataProvider(name = "rebalanceAllocations")
public Object[][] rebalanceAllocations() {
    return new Object[][] {
        { "CONSERVATIVE", 60, 30, 10 },    // 60% bonds, 30% stocks, 10% cash
        { "BALANCED", 40, 50, 10 },        // 40% bonds, 50% stocks, 10% cash
        { "AGGRESSIVE", 20, 70, 10 }       // 20% bonds, 70% stocks, 10% cash
    };
}

@Test(dataProvider = "rebalanceAllocations")
public void rebalancePreviewReflectsTargetAllocation(String profile, 
        int expectedBonds, int expectedStocks, int expectedCash) {
    // Test implementation using parameters
}
```

**Result**: Single method → 3 separate test cases with own pass/fail status.

**Interview Angle**: Test parameterization, reducing code duplication.

---

### 7. **JSON Response Parsing in Assertions**

**Problem**: Brittle assertions on pretty-printed JSON strings fail with formatting changes.

**Solution**: Parse JSON into domain objects, assert on structure.

```java
// ❌ Brittle: depends on exact formatting
assertTrue(jsonResponse.contains("\"bonds\": 60"));

// ✅ Robust: parse to object
ObjectMapper mapper = new ObjectMapper();
AllocationResponse allocation = mapper.readValue(jsonResponse, AllocationResponse.class);
assertThat(allocation.getBonds()).isEqualTo(60);
```

**Interview Angle**: Test maintainability, understanding JSON handling.

---

## Key Components

### Component 1: WebDriverFactory

**Responsibility**: Manage WebDriver lifecycle (creation, thread safety, cleanup)

**Key Methods**:
```java
public static WebDriver getOrCreate()           // Lazy-init singleton
public static void quit()                       // Cleanup
private static boolean isHeadlessEnabled()      // Mode detection
private static WebDriver createWebDriver()      // Configuration
```

**Design Pattern**: Singleton with ThreadLocal

---

### Component 2: UiTestRuntime

**Responsibility**: Manage Spring application lifecycle for UI tests

**Key Methods**:
```java
public static void ensureAppStarted()           // Start app once per suite
public static int getApplicationPort()          // Get random port assigned
public static String getBaseUrl()               // Return http://localhost:PORT
public static void stopApplication()            // Cleanup
```

**Timing**:
- First test call: ~5-8 seconds (Spring boot startup)
- Subsequent tests: ~100ms each (app already running)

---

### Component 3: WealthDashboardPage

**Responsibility**: Encapsulate UI interactions (Page Object Model)

**Key Methods**:
```java
public void navigateToDashboard()               // Login/navigation
public void depositFunds(String amount)         // Form interaction
public void transferMoney(...)                  // Multi-step action
public PortfolioDetails rebalancePortfolio...() // Interaction + return object
```

**Pattern**: Fluent API where possible
```java
page.navigateToDashboard()
    .depositFunds("1000")
    .transferMoney("ACC001", "ACC002", "500")
    .assertPortfolioBalance(new BigDecimal("1500"));
```

---

### Component 4: CriticalUiIT (Cucumber Runner)

**Responsibility**: Execute BDD scenario on journey path

**Configuration**:
```java
@Suite
@SelectClasspathResource("features")
@IncludeTags("critical")
@IncludeEngines("cucumber")
public class CriticalUiIT {
    // Cucumber JUnit Platform Suite runner
}
```

**Feature File** (`critical_business_flow.feature`):
```gherkin
@critical
Scenario: Complete wealth management journey
    Given customer is onboarded
    And customer has two accounts
    When customer deposits 1000 USD
    And customer transfers 500 USD between accounts
    Then portfolio rebalance preview should shift to balanced allocation
```

**Steps**: Defined in `com.interview.wealthapi.uitest` via Cucumber step definitions

---

### Component 5: CriticalBusinessUiNgIT (TestNG Data-Driven)

**Responsibility**: Execute business rule validations with parameterized inputs

**Structure**:
```java
@Test(dataProvider = "rebalanceAllocations")
public void rebalancePreviewReflectsTargetAllocation(...) { }

@Test(dataProvider = "idempotentTransfers")
public void duplicateTransferRequestIsIgnored(...) { }

@Test(dataProvider = "negativeScenarios")
public void validateNegativeRules(...) { }
```

**Coverage**:
- 3 rebalance profiles (positive cases)
- 2 idempotent request scenarios (positive edge cases)
- 4 negative validation rules (rejection scenarios)
- **Total**: 9 test cases from 3 DataProvider methods

---

## Interview Talking Points

### 1. **Architecture & Multi-Module Strategy**

**Question**: "Walk us through your test architecture."

**Answer**:
> "We have a three-module Maven structure: 
> - **api/** contains the Spring Boot application and runs API-level integration tests using REST Assured against live endpoints. Tests start the app on a random port, validate REST contract, and database interactions.
> - **ui-tests/** runs end-to-end Selenium tests in TWO parallel modes: Cucumber BDD for journey validation and TestNG data-driven for edge case coverage. Dual Failsafe execution lets us maintain separate test strategies in one module.
> - **db-tests/** uses Testcontainers to spin up real PostgreSQL containers and validate repository layer JPA operations.
>
> This layering allows us to fail fast at lower levels (unit → API → UI) and gives clear separation of concerns for maintenance."

**Why It Matters**: Shows understanding of test pyramid, modular Maven design, and practical separation of responsibilities.

---

### 2. **Local Developer Experience**

**Question**: "How do you balance automation with developer workflow?"

**Answer**:
> "Default behavior (no CLI flags) boots the app with a **visible browser** for local dev. This is critical: developers see their tests run in real Chrome, can debug visually, and don't need to tail logs. 
>
> The WebDriverFactory checks `ui.headless` property—default false. In CI (GitHub Actions, Jenkins), we set CI=true or explicitly pass `-Dui.headless=true`, which enables headless mode for speed and no GUI overhead.
>
> We also use Cucumber for journey storytelling (single comprehensive scenario) and TestNG DataProvider for edge cases. This combo satisfies both manual testing validation ('does the happy path work?') and regression ('do all edge cases fail correctly?')."

**Why It Matters**: Shows empathy for developer productivity, understanding CI/local differences, and practical UX.

---

### 3. **Test Parameterization Strategy**

**Question**: "How do you handle testing multiple scenarios without duplication?"

**Answer**:
> "We use TestNG's `@DataProvider` annotation extensively. For rebalance testing, we have ONE method annotated `@Test(dataProvider = "rebalanceAllocations")` that receives three parameter sets:
> - Conservative (60/30/10 bonds/stocks/cash)
> - Balanced (40/50/10)
> - Aggressive (20/70/10)
>
> This generates three INDEPENDENT test cases automatically—they each show as separate results in reports. If one fails, others still run. Same approach for negative cases: single method, 4 parameter sets (negative amount, zero deposit, insufficient balance, same-account transfer), yielding 4 test results.
>
> The key is that each scenario is a complete boundary test—we're not just checking happy path, but validating rejection behavior against business rules."

**Why It Matters**: Demonstrates DRY principle, understanding parameterization tools, and thoughtful edge case coverage.

---

### 4. **CI/CD Integration**

**Question**: "How does this fit into your CI pipeline?"

**Answer**:
> "Maven's Failsafe plugin handles integration test phases automatically during `mvn verify`. 
>
> In CI:
> - We set `CI=true` environment variable, which auto-enables headless mode
> - All tests run headless (~1.5 min for full suite)
> - Surefire and Failsafe reports are collected and published to the CI dashboard
> - If any Failsafe test fails, build fails (exit code 1), preventing merge
>
> Locally, developers do `mvn verify` with visible browser, see failures immediately, and push only when all green. This gives fast local feedback + reliable CI gates."

**Why It Matters**: Shows understanding of CI/CD practices, Maven build lifecycle, and environment-aware testing.

---

### 5. **Thread Safety & WebDriver Management**

**Question**: "How do you manage WebDriver instances in a multi-threaded environment?"

**Answer**:
> "WebDriverFactory uses ThreadLocal pattern with synchronized lazy initialization. Each thread gets its own WebDriver instance stored in ThreadLocal, preventing cross-contamination.
>
> ```java
> private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
> 
> public static synchronized WebDriver getOrCreate() {
>     if (driver.get() == null) {
>         driver.set(createWebDriver());
>     }
>     return driver.get();
> }
> ```
>
> When a test thread terminates or cleanup is called, we invoke `driver.remove()`, ensuring the WebDriver is quit and ThreadLocal is cleared. This prevents memory leaks and isolated test execution—one thread's Chrome instance doesn't interfere with another's."

**Why It Matters**: Multi-threading is complex; ThreadLocal shows deep understanding of concurrency in test automation.

---

### 6. **Page Object Model & Maintainability**

**Question**: "How do you structure UI test code to survive UI changes?"

**Answer**:
> "We use the Page Object Model pattern: UI selectors and interactions are encapsulated in `WealthDashboardPage.java`, not scattered across test files.
>
> If the submit button changes from `id='submitBtn'` to `class='action-button'`, we update ONE place—the By selector in WealthDashboardPage—and all 30 tests using that button automatically adapt.
>
> Methods are named after business operations ('depositFunds', 'transferMoney', 'rebalancePortfolio'), not technical actions. Tests read like requirements:
> ```java
> page.depositFunds("1000")
>     .transferMoney("ACC001", "ACC002", "500");
> ```
>
> This separation makes tests resilient to UI refactoring and dramatically reduces maintenance burden."

**Why It Matters**: Shows scaling mindset, understanding long-term test maintainability.

---

### 7. **Assertion Strategies**

**Question**: "How do you assert on complex API responses without brittle string matching?"

**Answer**:
> "We parse JSON into typed objects using ObjectMapper, then assert on structure, not formatting.
>
> ❌ **Brittle**:
> ```java
> assertTrue(response.contains("\"bonds\": 60"));
> ```
>
> ✅ **Robust**:
> ```java
> AllocationResponse allocation = mapper.readValue(response, AllocationResponse.class);
> assertThat(allocation.getBonds()).isEqualTo(60);
> ```
>
> The second approach survives JSON reformatting, whitespace changes, or reordering of fields. We're asserting on MEANING (the bonds value IS 60), not representation."

**Why It Matters**: Understanding assertion design prevents flaky tests and shows maturity in test automation.

---

### 8. **Handling Negative Test Cases**

**Question**: "How do you structure tests for failure scenarios?"

**Answer**:
> "Negative tests are first-class citizens in our suite. For each positive business rule ('transfer succeeds'), we have corresponding negative cases ('transfer fails for correct reasons').
>
> Our TestNG DataProvider includes a `negativeScenarios` with 4 cases:
> 1. **Negative amount**: Service normalizer rejects with 'Amount must be greater than zero'
> 2. **Zero deposit**: Validator rejects as duplicate/redundant
> 3. **Insufficient balance**: Business logic prevents overdraft
> 4. **Same-account transfer**: Validation rejects as nonsensical
>
> Each case is a complete assertion: UI submits malformed request → API returns specific error code (400/422) → error message matches contract. We're validating not just 'failure happens' but 'correct failure reason'."

**Why It Matters**: Shows thorough testing mindset, understanding boundary conditions.

---

### 9. **Test Performance & Optimization**

**Question**: "How do you keep UI tests fast when browser startup is expensive?"

**Answer**:
> "We optimize in layers:
>
> 1. **Shared App Startup**: UiTestRuntime boots the Spring app ONCE per test suite (5-8 seconds), not per test. All tests reuse the same running instance.
>
> 2. **Headless Mode**: Local: visible browser for visibility. CI: headless Chrome is 2-3x faster (no window rendering overhead).
>
> 3. **Targeted Execution**: Developers run just the failing test or specific module, not full suite. We document: `mvn -pl ui-tests verify` → full UI suite, or `mvn test` → fast unit feedback first.
>
> 4. **Test Isolation**: Each test is independent; no shared state. We can theoretically parallelize, though currently sequential for simplicity.
>
> Result: Local UI test suite ~2 min, CI ~1.5 min headless. Given the value (journey validation + edge case coverage), this is acceptable."

**Why It Matters**: Practical understanding of performance trade-offs in test automation.

---

### 10. **Reporting & Observability**

**Question**: "How do you track test results and failures?"

**Answer**:
> "We generate multiple reports:
>
> 1. **Surefire Reports** (Unit tests): HTML summaries in `target/surefire-reports/`
> 2. **Failsafe Reports** (Integration tests): XML + text in `target/failsafe-reports/`
> 3. **Cucumber Reports**: Pretty-printed scenarios (pass/fail/pending) in `target/cucumber-reports/`
> 4. **Allure Reports** (if configured): Rich interactive dashboard with timelines, history, flake detection
>
> In CI, we publish these to the build dashboard. Developers can see not just 'build failed' but which specific test, what assertion failed, and sometimes captured logs for debugging."

**Why It Matters**: Shows understanding of observability, CI integration, and post-mortem analysis.

---

## Conclusion

This framework balances:
- **Developer Experience**: Visible browser, fast unit tests, clear docs
- **Coverage**: Journey tests (Cucumber) + edge cases (TestNG) + API contract (REST Assured) + DB (Testcontainers)
- **Scalability**: Modular Maven, Page Object Model, parameterized tests
- **Maintainability**: Clear folder structure, documented commands, design patterns

**For Interviews**: Use this guide to explain not just WHAT the framework does, but WHY each decision was made and how it solves real problems in test automation.

---

## Allure Report Integration (Optional)

**Status**: ✅ Dependencies configured, ⏳ Listeners auto-discovery pending

Allure provides rich, interactive test dashboards. For detailed setup instructions and troubleshooting, see [ALLURE_SETUP.md](ALLURE_SETUP.md).

**Quick Start**:
```bash
mvn -pl ui-tests -am verify
allure generate target/allure-results -o target/allure-report --clean
open target/allure-report/index.html
```

**Note**: If Allure results are not generated, ensure:
- Test listeners are discovered from classpath
- Tests are executed in same JVM as listener registration
- `allure-results/` directory exists after test run

---

### Quick Command Reference

```bash
# Local development
mvn verify                                    # All tests, visible browser

# Fast feedback during dev
mvn test                                      # Unit tests only

# Focus areas
mvn -pl ui-tests -am verify                  # UI tests only
mvn -pl api -am test                         # API tests only
mvn -pl db-tests -am verify                  # DB tests only

# CI environment
mvn verify -Dui.headless=true                # All tests, headless

# Specific test types
mvn -Dit.test=CriticalUiIT verify            # Cucumber only
mvn -Dit.test=CriticalBusinessUiNgIT verify  # TestNG only

# View reports
open target/surefire-reports/index.html      # Unit test report
open target/failsafe-reports/index.html      # Integration report
open target/cucumber-reports/index.html      # Cucumber report
```

---

**Document Version**: 1.0  
**Last Updated**: March 2026  
**Audience**: Developers, QA, Interviewers, Team Leads
