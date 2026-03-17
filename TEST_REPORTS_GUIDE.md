# Test Reports Quick Reference

## After Running `mvn -pl ui-tests -am verify`

### 🟢 Available Reports (Working Now)

#### 1. Cucumber BDD Report (Recommended for Journey Tests)
```bash
open target/cucumber-reports/ui-critical.html
```
- **Contains**: BDD scenario execution, step-by-step flow
- **Best for**: Demonstrating happy-path journey, business flow clarity
- **Format**: Interactive HTML

#### 2. TestNG/Failsafe XML Reports (Integration Tests)
```bash
ls target/failsafe-reports/*.xml
```
- **Contains**: Test execution summaries, test counts, pass/fail status
- **Files**: 
  - `TEST-com.interview.wealthapi.uitest.CriticalBusinessUiNgIT.xml`
  - `failsafe-summary.xml`
- **Best for**: CI integration, automated report parsing
- **Format**: Structured XML

#### 3. Surefire Unit Test Reports
```bash
ls target/surefire-reports/*.xml
open target/surefire-reports/index.html
```
- **Contains**: API unit test results
- **Best for**: Quick unit test coverage overview
- **Format**: HTML (if index.html exists) or XML

---

### 🔵 Optional Enhanced Reports

#### Allure Interactive Dashboard
```bash
# Step 1: Run tests (if not already done)
mvn -pl ui-tests -am verify

# Step 2: Generate Allure report
allure generate target/allure-results -o target/allure-report --clean

# Step 3: Open report
open target/allure-report/index.html
```
- **Contains**: Timelines, test execution trends, failure analysis
- **Best for**: Comprehensive test metrics and dashboards
- **Requires**: `brew install allure` (CLI tool)
- **See**: [ALLURE_SETUP.md](ALLURE_SETUP.md) for troubleshooting

---

## Report Details by Test Type

### Cucumber Journey Test (BDD)
**File**: `target/cucumber-reports/ui-critical.html`

**Shows**:
- Single comprehensive scenario with multiple steps
- Clear Given-When-Then structure
- Step-by-step execution flow
- Useful for explaining business process

**Example**:
```
Feature: Wealth Dashboard Journey
  Scenario: Customer onboarding and wealth management flow
    Given customer is registered
    When customer deposits funds
    And customer transfers between accounts
    Then portfolio shows updated balance
    And rebalance preview adjusts allocation
```

---

### TestNG DataProvider Tests (Parameterized)
**File**: `target/failsafe-reports/TEST-com.interview.wealthapi.uitest.CriticalBusinessUiNgIT.xml`

**Shows**:
- 9 parameterized test cases
- Pass/Fail status for each case
- Execution time per test
- Individual assertion details

**Coverage**:
- 3 Rebalance scenarios
- 2 Idempotent transfer tests
- 4 Negative validation rules

**Example**:
```xml
<testcase classname="CriticalBusinessUiNgIT" 
          name="rebalancePreviewReflectsTargetAllocation[CONSERVATIVE]"
          time="5.234">
  <!-- PASSED -->
</testcase>
```

---

## Viewing Reports in Different Ways

### Option 1: Command Line (Fastest)
```bash
# Open in default browser
open target/cucumber-reports/ui-critical.html
open target/allure-report/index.html

# Or use specific browser
open -a Chrome target/cucumber-reports/ui-critical.html
```

### Option 2: VS Code Extension
1. Install "Live Server" extension
2. Right-click on HTML report file
3. Select "Open with Live Server"

### Option 3: Simple HTTP Server
```bash
cd target/cucumber-reports
python3 -m http.server 8000
# Visit http://localhost:8000/ui-critical.html
```

---

## Report Interpretation

### Green/Red Light Indications

| Report | ✅ Success | ❌ Failure |
|--------|-----------|-----------|
| **Cucumber** | All steps green | Any step red/yellow |
| **TestNG** | All test cases passed | Any test case failed |
| **Allure** | High pass %, no flaky | Low pass %, flaky count |

### Key Metrics to Check

**Cucumber Report**:
- All steps executed
- Execution time reasonable (< 2 min for full journey)
- No pending or skipped steps (yellow)

**TestNG/Failsafe Report**:
- Total Tests = Expected count (should be 9)
- Failures = 0
- Errors = 0
- Skipped = 0
- Success Rate = 100%

**Allure Report**:
- Pass rate in green
- Timeline shows reasonable distribution
- No "Flaky" category
- No repeated failures

---

## Continuous Integration Context

In CI/CD pipelines (GitHub Actions, Jenkins):

```bash
# Run tests quietly, capture reports
mvn -pl ui-tests -am -B verify

# Parse TestNG/Failsafe XML for decision
EXIT_CODE=$?

# Generate enhanced report
if [ $EXIT_CODE -eq 0 ]; then
  allure generate target/allure-results -o target/allure-report --clean
  echo "Report URL: ${BUILD_URL}allure-report/index.html"
fi
```

---

## File Organization

```
target/
├── cucumber-reports/          # Cucumber HTML report (opened via browser)
│   └── ui-critical.html       # BDD journey test results
├── failsafe-reports/          # Failsafe XML reports (parsed by tools)
│   ├── TEST-*.xml             # Individual test class results
│   ├── failsafe-summary.xml   # Summary of all tests
│   └── *.txt                  # Test execution logs
├── surefire-reports/          # Surefire unit test results
│   ├── index.html             # HTML summary (if available)
│   └── TEST-*.xml             # Individual test class XML
├── allure-results/            # Allure JSON data (if tests configured)
│   └── *.json                 # Test execution JSON snapshots
└── allure-report/             # Allure HTML dashboard (generated)
    └── index.html             # Main report page
```

---

## For Interview Preparation

**Key Talking Points with These Reports**:

1. **Test Coverage**:
   - "We have X BDD scenarios covering the happy path"
   - "Plus Y parameterized test cases covering edge cases and negative scenarios"

2. **Execution Insights**:
   - "Average test execution time is ~2 minutes for full suite"
   - "Individual test takes ~15-30 seconds for browser initialization"

3. **Evidence of Quality**:
   - "All 10 tests passing consistently (1 Cucumber + 9 TestNG)"
   - "No flaky tests or intermittent failures"

4. **CI Integration**:
   - "Reports are automatically generated and published to build dashboard"
   - "Developers see results in less than 3 minutes after commit"

---

## Quick Command Reference

```bash
# Run tests and generate all available reports
mvn -pl ui-tests -am verify

# View Cucumber report immediately
open target/cucumber-reports/ui-critical.html

# Generate Allure report (if allure CLI installed)
allure generate target/allure-results -o target/allure-report --clean
open target/allure-report/index.html

# List all test result files
find target -name "*.xml" -o -name "*.html" | grep -E "test|report"
```

---

**Document Version**: 1.0  
**Last Updated**: March 2026  
**Audience**: Developers, QA Engineers, Interviewers
