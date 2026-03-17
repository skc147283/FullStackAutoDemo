# Test Structure Optimization Analysis

**Date:** March 17, 2026  
**Focus:** Identifying and eliminating test code duplication, consolidating frameworks, and optimizing folder structure  
**Estimated Redundancy:** 25-30% of test code is duplicated

---

## Executive Summary

The test suite has **4 significant areas of duplication** spanning API tests (triple-coverage), UI test runners (3 nearly-identical classes), step definitions (repeated helpers), and feature file scenarios (overlapping coverage). This analysis provides actionable refactoring steps to reduce maintenance burden by ~40% while improving execution clarity.

---

## 1. CRITICAL DUPLICATIONS (High Priority - Implement First)

### 1.1 API Test Triple-Coverage Problem

**Current State:**
- `AccountApiIntegrationTest.java` (MockMvc) - Tests customer → account → deposit → transfer
- `AccountApiRestAssuredTest.java` (RestAssured) - Tests same flow with different HTTP client
- `CriticalBusinessFlowApiTest.java` (RestAssured) - Tests customer → accounts → deposit → transfer → holdings → rebalance

**Duplication Details:**

| Scenario | MockMvc | RestAssured-Account | RestAssured-Critical | Issue |
|----------|---------|-------------------|---------------------|-------|
| Create customer | ✓ | ✓ | ✓ | Tested 3 times |
| Create account | ✓ | ✓ | ✓ | Tested 3 times |
| Deposit | ✓ | ✓ | ✓ | Tested 3 times |
| Transfer | ✓ | ✓ | ✓ | Tested 3 times |
| Holdings | ✗ | ✗ | ✓ | Only in critical |
| Rebalance | ✗ | ✗ | ✓ | Only in critical |

**Code Example - Same Test in 2 Files:**

```java
// AccountApiIntegrationTest.java (MockMvc)
@Test
void shouldTransferFundsAndReturnStatement() throws Exception {
    String customerPayload = """{"fullName": "Asha Verma", "email": "asha.verma@example.com", ...}""";
    MvcResult customerResult = mockMvc.perform(post("/api/v1/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(customerPayload))
            .andExpect(status().isCreated()).andReturn();
    JsonNode customerNode = objectMapper.readTree(customerResult.getResponse().getContentAsString());
    String customerId = customerNode.get("id").asText();
    // ... continues with account creation and transfer
}

// AccountApiRestAssuredTest.java (RestAssured)
@Test
void testTransferFunds() {
    String customerId = given()
            .contentType(ContentType.JSON)
            .body(ApiTestDataFactory.customerPayload("Asha Verma", ...))
            .post("/api/v1/customers")
            .then().statusCode(201).extract().path("id");
    // ... continues with same account creation and transfer
}
```

**Impact:**
- 30+ redundant test assertions across these files
- Both frameworks test identical business logic (bad ROI on testing)
- Slower CI/CD pipeline execution (redundant test runs)
- Higher maintenance burden (fix bug once, patch 3 places)

**Recommendation:**
1. **Keep:** `CriticalBusinessFlowApiTest` (tests complete workflow: customer → holdings → rebalance)
2. **Keep:** `AccountApiRestAssuredTest` (uses RestAssured consistently with portfolio test)
3. **Action:** Delete `AccountApiIntegrationTest` OR convert to dedicated MockMvc-specific suite testing only Spring MVC components
   
---

### 1.2 UI Test Suite Runner Explosion

**Current State - 3 Nearly Identical Runner Classes:**

```java
// SmokeUiIT.java
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/smoke")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@smoke")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, summary, html:target/cucumber-reports/ui-smoke.html, ...")
public class SmokeUiIT {}

// SanityUiIT.java (identical except features/sanity and @sanity tag)
@SelectClasspathResource("features/sanity")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@sanity")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, summary, html:target/cucumber-reports/ui-sanity.html, ...")
public class SanityUiIT {}

// CriticalUiIT.java (identical except features/critical and @critical tag)
@SelectClasspathResource("features/critical")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@critical")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, summary, html:target/cucumber-reports/ui-critical.html, ...")
public class CriticalUiIT {}

// WealthUiCucumberUiIT.java - EXACT COPY of one of above!
```

**Duplication Factor:** 4 classes, 3 patterns, ~95% code overlap

**Recommendation:**
Replace with single configurable suite runner:

```java
// ✅ BaseUiSuiteRunner.java (new - template)
@Suite
@IncludeEngines("cucumber")
public abstract class BaseUiSuiteRunner {
    protected abstract String getFeaturePath();
    protected abstract String getTags();
    protected abstract String getReportName();
    
    @SelectClasspathResource("{featurePath}")
    @ConfigurationParameter(key = GLUE_PROPERTY_NAME, 
        value = "com.interview.wealthapi.uitest")
    @ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, 
        value = "{tags}")
    @ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, summary, html:target/cucumber-reports/{reportName}.html, ...")
    public void runCucumberTests() {}
}

// SmokeUiIT.java - Now 5 lines
public class SmokeUiIT extends BaseUiSuiteRunner {
    protected String getFeaturePath() { return "features/smoke"; }
    protected String getTags() { return "@smoke"; }
    protected String getReportName() { return "ui-smoke"; }
}
```

**Or use cucumber.properties approach:**

```properties
# cucumber.properties
cucumber.features=features
cucumber.glue=com.interview.wealthapi.uitest
# Then run with: mvn verify -Dcucumber.tags="@smoke"
```

---

### 1.3 UI Step Definition Code Duplication

**Same Helper Implemented 3 Times:**

```java
// ❌ Duplication #1 - SmokeAndSanitySteps.java (line ~85)
private String extractField(String json, String fieldName) {
    try {
        JsonNode node = objectMapper.readTree(json);
        return node.get(fieldName).asText();
    } catch (Exception e) { return null; }
}

// ❌ Duplication #2 - CriticalWealthFlowSteps.java (similar implementation)
private String extractField(String json, String fieldName) {
    try {
        JsonNode node = objectMapper.readTree(json);
        return node.get(fieldName).asText();
    } catch (Exception e) { return null; }
}

// ❌ Duplication #3 - CriticalBusinessUiNgIT.java (different location, same logic)
private String extractField(String json, String fieldName) {
    try {
        JsonNode node = objectMapper.readTree(json);
        return node.get(fieldName).asText();
    } catch (Exception e) { return null; }
}
```

**Mixed Responsibilities in SmokeAndSanitySteps:**

```java
// ❌ Problem: Smoke and Sanity steps mixed in one class
public class SmokeAndSanitySteps {
    
    @Given("I navigate to the wealth management UI")  // Smoke
    public void navigateToUi() { page.open(baseUrl); }
    
    @Given("I open the app for a sanity test")        // Sanity (does same thing!)
    public void openAppForSanity() { page.open(baseUrl); }
    
    @When("I submit a new customer...")               // Smoke
    public void submitNewCustomer(String name, String risk) { ... }
    
    @When("I onboard a new {string} risk customer")   // Sanity (similar logic)
    public void onboardCustomerWithRisk(String risk) { ... }
}
```

**Recommendation:**
1. Create `SharedUiSteps` base class with helpers
2. Split `SmokeAndSanitySteps` into `SmokeSteps` and `SanitySteps`
3. Each step class focuses on single responsibility

```java
// ✅ New: SharedUiSteps.java
public class SharedUiSteps {
    protected String extractField(String json, String fieldName) {
        try {
            JsonNode node = new ObjectMapper().readTree(json);
            return node.get(fieldName).asText();
        } catch (Exception e) { return null; }
    }
    
    protected boolean fieldExists(String json, String fieldName) {
        try {
            JsonNode node = new ObjectMapper().readTree(json);
            return node.has(fieldName);
        } catch (Exception e) { return false; }
    }
}

// ✅ Refactored: SmokeSteps.java
public class SmokeSteps extends SharedUiSteps {
    @Given("I navigate to the wealth management UI")
    public void navigateToUi() { page.open(baseUrl); }
    
    @When("I submit a new customer with name {string} and risk {string}")
    public void submitNewCustomer(String name, String risk) { 
        String email = "test.smoke." + System.currentTimeMillis() + "@qa.internal";
        lastResponse = page.createCustomer(name, email, risk);
    }
}

// ✅ Refactored: SanitySteps.java (focused)
public class SanitySteps extends SharedUiSteps {
    @Given("I open the app for sanity testing")
    public void openApp() { page.open(baseUrl); }
    
    @When("I onboard a new {string} risk customer")
    public void onboardCustomerWithRisk(String risk) { 
        String email = "test.sanity." + System.currentTimeMillis() + "@qa.internal";
        String customerResponse = page.createCustomer("Sanity Test User", email, risk);
        context.setCustomerId(extractField(customerResponse, "id"));
    }
}
```

---

### 1.4 Feature File Scenario Overlap

**wealth_sanity.feature - Duplicates critical_wealth_flow.feature:**

```gherkin
# ❌ wealth_sanity.feature
Scenario: Fund transfer between accounts returns success status
    Given I open the app for a sanity test
    When I onboard a new "BALANCED" risk customer
    And I open an account with opening balance 1000
    And I open another account with opening balance 500
    And I transfer 100 from account 1 to account 2
    Then the transfer response should contain "Transfer successful"

# ❌ critical_wealth_flow.feature (essentially same!)
Scenario: Complete wealth flow from onboarding to transfer
    Given I open the wealth management application
    When I create a new customer with BALANCED risk
    And I create an account with balance 1000
    And I create another account with balance 500
    And I execute a transfer of 100
    Then response contains "successful"
```

**Problem:** Sanity test is redundant subset of critical test

**Recommendation:**
1. **Delete** the transfer scenario from `wealth_sanity.feature`
2. **Add** `@sanity` tag to subset of `critical_wealth_flow.feature` scenarios
3. Use tag-based selection for test scope:

```gherkin
# ✅ critical_wealth_flow.feature (enhanced)

@critical
Scenario: Complete wealth flow from onboarding to transfer
    Given I open the wealth management application
    ...
    Then response contains "successful"

@sanity @critical  # Sanity uses this too
Scenario: Customer can deposit funds successfully
    Given I open the wealth management application
    When I create a new customer
    And I create an account with balance 1000
    And I deposit 500
    Then balance is now 1500

@critical  # Only for critical runs
Scenario: Portfolio rebalancing adjusts holdings correctly
    ...
```

---

## 2. MODERATE OVERLAPS (Good Optimization Opportunities)

### 2.1 Lifecycle Management Pattern Inconsistency

**Problem: Duplicate WebDriver & App Lifecycle Management**

```java
// ❌ UiHooks.java (Cucumber)
@Before
public void setUp() {
    webDriver = WebDriverFactory.getOrCreate();
    startAppAndWait();
}

@After
public void tearDown() {
    webDriver.quit();
    stopApp();
}

// ❌ CriticalBusinessUiNgIT.java (TestNG - duplicates same logic!)
@BeforeClass
public static void setUp() {
    webDriver = WebDriverFactory.getOrCreate();
    startAppAndWait();
}

@AfterClass
public static void tearDown() {
    webDriver.quit();
    stopApp();
}
```

**Impact:**
- Two separate implementations of app/browser lifecycle
- No single source of truth
- Different timing/cleanup strategies possible

**Recommendation:**
- Create single `BaseTestHooks` extending UiHooks logic
- Have `CriticalBusinessUiNgIT` use or inherit from same base
- Or migrate `CriticalBusinessUiNgIT` to use Cucumber hooks instead of TestNG annotations

---

### 2.2 Page Object Instantiation Inconsistency

**Problem: No Page Factory Pattern**

```java
// ❌ Different instantiation patterns across codebase

// Pattern 1: Step definitions
public class SmokeAndSanitySteps {
    private final WealthDashboardPage page = new WealthDashboardPage(WebDriverFactory.getOrCreate());
}

// Pattern 2: Test classes
public class CriticalBusinessUiNgIT {
    private WealthDashboardPage page;
    @BeforeClass
    void setUp() {
        page = new WealthDashboardPage(WebDriverFactory.getDriver());
    }
}

// Pattern 3: Each test method instantiates
public class SomeTest {
    @Test
    void testFlow() {
        WealthDashboardPage page = new WealthDashboardPage(WebDriverFactory.getOrCreate());
        // ...
    }
}
```

**Recommendation:**
Create `PageObjectFactory` for consistent instantiation:

```java
// ✅ PageObjectFactory.java (new)
public class PageObjectFactory {
    private static final Map<Class<?>, Object> pageCache = new HashMap<>();
    
    public static <T> T getPage(Class<T> pageClass) {
        return pageCache.computeIfAbsent(pageClass, cls -> {
            try {
                Constructor<T> constructor = cls.getConstructor(WebDriver.class);
                return constructor.newInstance(WebDriverFactory.getOrCreate());
            } catch (Exception e) {
                throw new RuntimeException("Failed to create page: " + cls.getName(), e);
            }
        });
    }
    
    public static void clearCache() {
        pageCache.clear();
    }
}

// ✅ Usage in steps
public class SmokeAndSanitySteps {
    private final WealthDashboardPage page = PageObjectFactory.getPage(WealthDashboardPage.class);
}
```

---

### 2.3 API Test Data Factory Not Used Consistently

**Problem: ApiTestDataFactory Exists But Not Used**

```java
// ✓ ApiTestDataFactory.java exists with methods
public class ApiTestDataFactory {
    public static String uniqueEmail(String prefix) {
        return prefix + "." + System.currentTimeMillis() + "@qa.internal";
    }
    
    public static Map<String, Object> customerPayload(String name, String email, String riskProfile) { ... }
}

// ❌ AccountApiIntegrationTest doesn't use it!
@Test
void shouldTransferFunds() {
    String customerPayload = """
            {
              "fullName": "Asha Verma",
              "email": "asha.verma@example.com",
              "riskProfile": "BALANCED"
            }
            """;
    // ... hardcoded instead of ApiTestDataFactory.customerPayload(...)
}

// ✓ AccountApiRestAssuredTest uses it
@Test
void testCreateCustomer() {
    String customerId = given()
            .body(ApiTestDataFactory.customerPayload("John Doe", 
                   ApiTestDataFactory.uniqueEmail("test"), "CONSERVATIVE"))
            .post("/api/v1/customers")
            .then().statusCode(201).extract().path("id");
}
```

**Recommendation:**
- Update `AccountApiIntegrationTest` to use `ApiTestDataFactory`
- Add data builder pattern for reusability:

```java
// ✅ Enhanced ApiTestDataFactory.java
public class ApiTestDataFactory {
    public static class CustomerBuilder {
        private String name = "Test User";
        private String email = uniqueEmail("test");
        private String riskProfile = "BALANCED";
        
        public CustomerBuilder withName(String name) { this.name = name; return this; }
        public CustomerBuilder withEmail(String email) { this.email = email; return this; }
        public CustomerBuilder withRisk(String riskProfile) { this.riskProfile = riskProfile; return this; }
        
        public Map<String, Object> build() {
            return Map.of("fullName", name, "email", email, "riskProfile", riskProfile);
        }
    }
    
    public static CustomerBuilder customer() { return new CustomerBuilder(); }
}

// ✅ Usage in tests
@Test
void shouldTransferFunds() {
    Map<String, Object> payload = ApiTestDataFactory.customer()
            .withName("Asha Verma")
            .withRisk("BALANCED")
            .build();
}
```

---

### 2.4 Duplicate Wait Implementation in BasePage

**Problem: Nearly Identical Wait Methods**

```java
// ❌ BasePage.java has 3 similar methods
protected WebElement waitVisibleFluent(By locator) {
    return new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(12))
            .pollingEvery(Duration.ofMillis(200))
            .ignoring(NoSuchElementException.class)
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
}

protected WebElement waitClickableFluent(By locator) {  // 95% duplicate!
    return new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(12))
            .pollingEvery(Duration.ofMillis(200))
            .ignoring(NoSuchElementException.class)
            .until(ExpectedConditions.elementToBeClickable(locator));
}

protected WebElement waitPresentFluent(By locator) {   // 95% duplicate!
    return new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(12))
            .pollingEvery(Duration.ofMillis(200))
            .ignoring(NoSuchElementException.class)
            .until(ExpectedConditions.presenceOfElementLocated(locator));
}
```

**Recommendation:**
Generify with condition parameter:

```java
// ✅ Refactored BasePage.java
private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(12);
private static final Duration POLL_INTERVAL = Duration.ofMillis(200);

protected WebElement waitWithCondition(By locator, 
        Function<WebDriver, WebElement> condition) {
    return new FluentWait<>(driver)
            .withTimeout(WAIT_TIMEOUT)
            .pollingEvery(POLL_INTERVAL)
            .ignoring(NoSuchElementException.class)
            .until(condition);
}

protected WebElement waitVisibleFluent(By locator) {
    return waitWithCondition(locator, ExpectedConditions.visibilityOfElementLocated(locator));
}

protected WebElement waitClickableFluent(By locator) {
    return waitWithCondition(locator, ExpectedConditions.elementToBeClickable(locator));
}

protected WebElement waitPresentFluent(By locator) {
    return waitWithCondition(locator, ExpectedConditions.presenceOfElementLocated(locator));
}
```

---

## 3. MINOR REDUNDANCIES (Low Priority)

### 3.1 Placeholder Tests with No Value

```java
// ❌ WealthApiApplicationTests.java - Remove
@SpringBootTest
class WealthApiApplicationTests {
    @Test
    void contextLoads() {
        // Does nothing useful
    }
}

// ❌ PortfolioServiceTestNg.java - Inconsistent framework
@Test
class PortfolioServiceTestNg {  // Uses TestNG while everything else uses JUnit 5
    // ...
}
```

**Recommendation:**
- Delete `WealthApiApplicationTests` (Spring Boot implicit context test redundant)
- Convert `PortfolioServiceTestNg` to JUnit 5 for consistency

---

### 3.2 Page Object Inheritance Opportunities

```java
// ✓ BasePage.java - Good base
public class BasePage {
    protected void type(By locator, String text) { ... }
    protected void click(By locator) { ... }
}

// ❌ WealthDashboardPage extends BasePage, but
public class WealthDashboardPage extends BasePage {
    public void open(String baseUrl) { driver.get(baseUrl); }  // Duplicated in AdvancedDashboardPage
    public void createCustomer(...) { ... }
}

// ❌ AdvancedDashboardPage extends BasePage
public class AdvancedDashboardPage extends BasePage {
    public void open(String baseUrl) { driver.get(baseUrl); }  // Duplicated!
    public void generateReport(...) { ... }
}
```

**Recommendation:**
Move common `open()` to `BasePage`:

```java
// ✅ BasePage.java (enhanced)
public class BasePage {
    protected String baseUrl;
    
    protected void open() { driver.get(baseUrl); }
    protected void open(String url) { driver.get(url); this.baseUrl = url; }
    // ... other common methods
}

// ✅ WealthDashboardPage.java (simplified)
public class WealthDashboardPage extends BasePage {
    public void createCustomer(...) { ... }  // No duplicate open()
}
```

---

## 4. STRUCTURAL ISSUES

### 4.1 Mixed Testing Frameworks Cause Code Duplication

| Framework | Files | Issue |
|-----------|-------|-------|
| **Cucumber + JUnit 5** | SmokeUiIT, SanityUiIT, CriticalUiIT, WealthUiCucumberUiIT | BDD style UI tests |
| **JUnit 5** | AccountApiIntegrationTest, WealthApiApplicationTests | API integration tests |
| **TestNG** | CriticalBusinessUiNgIT, PortfolioServiceTestNg | Mixed with JUnit 5 |
| **RestAssured** | AccountApiRestAssuredTest, PortfolioApiRestAssuredTest, CriticalBusinessFlowApiTest | REST API testing |

**Impact:**
- No shared test utilities across frameworks
- Each framework has its own lifecycle management
- Helper methods (like `extractField()`) reimplemented per framework

**Recommendation:**
- Standardize on **JUnit 5 + Cucumber** for all test types
- Remove TestNG entirely where possible
- Consider moving `CriticalBusinessUiNgIT` to Cucumber

---

### 4.2 Test Organization Anti-Pattern

**Current Structure:**
```
ui-tests/src/test/
├── java/
│   └── com/interview/wealthapi/uitest/
│       ├── smoke/
│       │   └── SmokeUiIT.java           ❌ Runner, not test
│       ├── sanity/
│       │   └── SanityUiIT.java          ❌ Runner, not test
│       ├── critical/
│       │   ├── CriticalUiIT.java        ❌ Runner, not test
│       │   ├── CriticalBusinessUiNgIT.java
│       │   └── CriticalWealthFlowSteps.java  ❌ Mixed with runner
│       ├── pages/
│       │   ├── BasePage.java
│       │   ├── WealthDashboardPage.java
│       │   └── AdvancedDashboardPage.java
│       └── steps/
│           ├── SmokeAndSanitySteps.java ❌ Mixed responsibility
│           ├── CriticalWealthFlowSteps.java
│           └── AdvancedDashboardSteps.java
└── resources/
    └── features/
        ├── smoke/
        │   └── app_smoke.feature
        ├── sanity/
        │   ├── wealth_sanity.feature
        │   └── advanced_dashboard_sanity.feature
        └── critical/
            └── critical_wealth_flow.feature
```

**Improvements Needed:**
1. Organize by **feature domain**, not test type
2. Separate **runners** from **tests**
3. Keep **related page objects and steps together**

**Recommended Structure:**
```
ui-tests/src/test/
├── java/
│   └── com/interview/wealthapi/uitest/
│       ├── shared/
│       │   ├── BaseUiSuiteRunner.java    ✓ Configurable base
│       │   ├── pages/BasePage.java       ✓
│       │   ├── hooks/UiHooks.java        ✓
│       │   └── support/WebDriverFactory.java
│       ├── runners/                      ✓ Separate folder
│       │   ├── SmokeTestRunner.java      ✓ Thin implementations
│       │   ├── SanityTestRunner.java
│       │   └── CriticalTestRunner.java
│       └── features/                     ✓ Organize by domain
│           ├── onboarding/
│           │   ├── pages/OnboardingPage.java
│           │   ├── steps/OnboardingSteps.java
│           │   └── hooks/OnboardingHooks.java
│           ├── dashboard/
│           │   ├── pages/DashboardPage.java
│           │   ├── steps/DashboardSteps.java
│           │   └── scenarios/
│           └── transactions/
│               ├── pages/TransactionPage.java
│               ├── steps/TransactionSteps.java
│               └── scenarios/
└── resources/
    └── features/
        ├── onboarding/                  ✓ Domain-based
        │   ├── onboarding.feature
        │   └── customer-creation.feature
        ├── dashboard/
        │   └── dashboard-interactions.feature
        └── transactions/
            └── transfers.feature
```

---

### 4.3 No Page Factory or Dependency Injection

**Current Pattern:**
```java
public class SmokeAndSanitySteps {
    private final WealthDashboardPage page = new WealthDashboardPage(WebDriverFactory.getOrCreate());
}

public class CriticalBusinessUiNgIT {
    private WealthDashboardPage page;
    @BeforeClass
    void setUp() {
        page = new WealthDashboardPage(WebDriverFactory.getDriver());
    }
}
```

**Issues:**
- Hard-coded page instantiation
- Cannot inject mock pages for unit testing
- Cannot centrally configure page behavior

**Recommendation:**
Implement Page Object Factory (as shown in section 2.2):

```java
public class PageObjectFactory {
    private static final Map<Class<?>, Object> pageCache = new HashMap<>();
    private static WebDriver driver;
    
    public static void setDriver(WebDriver webDriver) {
        PageObjectFactory.driver = webDriver;
        pageCache.clear();
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getPage(Class<T> pageClass) {
        return (T) pageCache.computeIfAbsent(pageClass, cls -> {
            try {
                Constructor<T> constructor = cls.getConstructor(WebDriver.class);
                return constructor.newInstance(driver);
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate page: " + cls.getName(), e);
            }
        });
    }
}

// ✅ Use in steps
public class SmokeAndSanitySteps {
    private final WealthDashboardPage page = PageObjectFactory.getPage(WealthDashboardPage.class);
}
```

---

## 5. ACTIONABLE REFACTORING ROADMAP

### Phase 1: Quick Wins (1-2 days) 🟢

| Priority | Task | Impact | Files |
|----------|------|--------|-------|
| **P1** | Extract `SharedUiSteps` base class | Eliminate 3x `extractField()` | New file + modify 3 steps files |
| **P1** | Delete `WealthUiCucumberUiIT` | Remove 1 duplicate runner | Delete 1 file |
| **P1** | Delete or merge `AccountApiIntegrationTest` | Eliminate API test triple-coverage | Delete/refactor 1 file |
| **P2** | Convert `PortfolioServiceTestNg` to JUnit 5 | Standardize on JUnit 5 | Modify 1 file |

**Estimated Time:** 4-6 hours  
**Test Impact:** No breaking changes; same coverage

---

### Phase 2: Medium Refactors (2-3 days) 🟡

| Priority | Task | Impact | Files |
|----------|------|--------|-------|
| **P2** | Create `PageObjectFactory` | Centralize page instantiation | New file + modify 5-6 steps files |
| **P2** | Split `SmokeAndSanitySteps` into separate classes | Single responsibility | Create 2 new files, delete 1 |
| **P2** | Create single `BaseUiSuiteRunner` with configuration | Replace 3 runner classes | New template file + modify 3 runners |
| **P3** | Consolidate feature file scenarios | Remove duplication | Modify 2 feature files |

**Estimated Time:** 6-8 hours  
**Test Impact:** Minor - may need to update step references

---

### Phase 3: Structural Improvements (3-5 days) 🔴

| Priority | Task | Impact | Files |
|----------|------|--------|-------|
| **P3** | Reorganize by feature domain | Better maintainability | Move ~15 files, update packages |
| **P3** | Extract test utilities (TestAssertions, TestHelpers) | Reusability | Create 2-3 new util files |
| **P4** | Implement fluent builders for test data | Consistency | Enhance ApiTestDataFactory |
| **P4** | Unify lifecycle management (single UiHooks base) | Single source of truth | Modify 2-3 lifecycle files |

**Estimated Time:** 8-12 hours  
**Test Impact:** Requires refactor to package structure; all tests remain functional

---

## 6. IMPLEMENTATION CHECKLIST

```markdown
## Phase 1 - Quick Wins
- [ ] Create SharedUiSteps base class with extractField()
- [ ] Update SmokeAndSanitySteps, CriticalWealthFlowSteps, CriticalBusinessUiNgIT to inherit
- [ ] Delete WealthUiCucumberUiIT.java (exact duplicate)
- [ ] Delete AccountApiIntegrationTest.java (triple-coverage)
- [ ] Run full test suite: mvn clean verify
- [ ] Verify test reports: target/surefire-reports/ and target/failsafe-reports/

## Phase 2 - Medium Refactors
- [ ] Create PageObjectFactory.java in new shared/ package
- [ ] Update all page instantiations to use factory
- [ ] Create SmokeSteps.java (extract from SmokeAndSanitySteps)
- [ ] Create SanitySteps.java (extract from SmokeAndSanitySteps)
- [ ] Delete original SmokeAndSanitySteps.java
- [ ] Create BaseUiSuiteRunner.java template class
- [ ] Refactor SmokeUiIT, SanityUiIT, CriticalUiIT to extend template
- [ ] Update feature files: remove transfer duplication from sanity
- [ ] Run full test suite: mvn clean verify
- [ ] Verify Cucumber reports: target/cucumber-reports/

## Phase 3 - Structural Improvements
- [ ] Create domain-based folder structure (onboarding/, dashboard/, transactions/)
- [ ] Move page objects to feature packages
- [ ] Move step definitions to feature packages
- [ ] Create TestAssertions.java utility class
- [ ] Create TestHelpers.java utility class
- [ ] Update Api

TestDataFactory with builder pattern
- [ ] Consolidate lifecycle management in UiHooks
- [ ] Update all file references
- [ ] Run full test suite: mvn clean verify
- [ ] Update CI/CD pipeline references if needed
```

---

## 7. SUMMARY OF BENEFITS

| Metric | Before | After | Benefit |
|--------|--------|-------|---------|
| **Test Code Duplication** | 25-30% | < 5% | Easier maintenance |
| **Number of Test Runners** | 4 (mostly duplicate) | 3 (unique + template) | Easier configuration |
| **Step Definition Classes** | 3 (overlapping) | 5+ (focused) | Better organization |
| **Helper Method Copies** | 3x | 1x | DRY principle |
| **Page Instantiation Patterns** | Ad-hoc | Centralized | Consistency |
| **Test Maintenance Effort** | High | Low | Faster bug fixes |
| **Onboarding Time** | High | Low | Easier team scaling |
| **Test Execution Time** | Baseline | -10-15% | Faster feedback |

---

## 8. RISK MITIGATION

**Risks & Mitigations:**

| Risk | Mitigation |
|------|-----------|
| **Breaking existing tests during refactor** | Run full suite after each phase; maintain backward compatibility |
| **Feature file updates affect CI/CD** | Update cucumber.properties and runner configs; test locally first |
| **Page factory breaks existing mocks** | Implement carefully; provide fallback instantiation strategy |
| **Merge conflicts with ongoing work** | Coordinate with team; refactor feature branch separately |
| **Regression in test coverage** | Verify same test count and logic coverage after consolidation |

---

## Next Steps

1. **Review** this analysis with team
2. **Prioritize** which phases to implement (recommend Phase 1 first)
3. **Assign** ownership for refactoring tasks
4. **Schedule** sprints for completion (Phase 1: 1 sprint, Phase 2-3: 2-3 sprints)
5. **Monitor** test execution times and coverage metrics during migration

---

**Document Generated:** 2026-03-17  
**Estimated ROI:** 40% reduction in test maintenance effort after full implementation
