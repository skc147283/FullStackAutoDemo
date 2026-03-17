# Allure Report Setup Guide

## Current Status

✅ **What's Working**:
- Cucumber BDD tests: Native HTML report (`target/cucumber-reports/ui-critical.html`)
- Surefire unit tests: Native XML reports (`target/surefire-reports/`)
- Failsafe integration tests: Native XML reports (`target/failsafe-reports/`)
- All test dependencies configured: `allure-cucumber7-jvm`, `allure-testng`

⏳ **What Needs Configuration**:
- Allure results directory (`target/allure-results/`) not being populated
- Allure listeners need to be explicitly discovered during test execution
- Allure HTML report generation

---

## Step 1: Verify Allure CLI Installation

```bash
# Check if Allure is installed
which allure

# If not installed, install via Homebrew
brew install allure
```

---

## Step 2: Ensure Test Listeners Are Configured

### For TestNG (Automatic):
The `allure-testng` dependency includes listeners that auto-register via Java SPI:
- No additional configuration needed if properly on classpath
- Listens to `@Test` method execution and `@DataProvider` calls

### For Cucumber (Automatic):
The `allure-cucumber7-jvm` dependency includes plugins that auto-register:
- Glue package needs to include Allure step listeners
- Cucumber hooks capture step execution data

### Verify Listener Detection:

Run with verbose logging to confirm listeners are loaded:
```bash
cd <repo-root>
mvn -pl ui-tests -am -X verify 2>&1 | grep -i "allure\|listener"
```

If you see references to `AllureTestNg`, `AllureCucumber`, listener discovery succeeded.

---

## Step 3: Run Tests with Allure Result Generation

If listeners are detected, tests will automatically create `target/allure-results/` with JSON files:

```bash
cd <repo-root>

# Run full test suite
mvn -pl ui-tests -am verify

# Verify allure-results directory was created
ls -la target/allure-results/
# Expected: *.json files with test case data
```

---

## Step 4: Generate Allure HTML Report

Once `target/allure-results/` contains JSON files:

### Using Allure CLI (Recommended):
```bash
# Generate report
allure generate target/allure-results -o target/allure-report --clean

# Open in browser
open target/allure-report/index.html
```

### Using Maven Plugin:
```bash
# Requires Allure repository configuration (see step 5)
mvn allure:report
open target/site/allure-report/index.html
```

---

## Step 5: (Optional) Configure Allure Maven Plugin Repository

If Maven plugin approach is preferred, add to parent `pom.xml`:

```xml
<repositories>
    <repository>
        <id>allure-mvn-repo</id>
        <url>https://repo.maven.apache.org/maven2</url>
    </repository>
</repositories>
```

Update plugin version to match available releases:
```xml
<plugin>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-maven</artifactId>
    <version>2.11.2</version>  <!-- Try older version if 2.12.0 fails -->
</plugin>
```

---

## Troubleshooting Allure Integration

### Issue: `target/allure-results/` not created

**Cause**: Listeners not on classpath or not auto-discovering

**Solutions**:
1. Verify dependencies are in `ui-tests/pom.xml`:
   ```xml
   <dependency>
       <groupId>io.qameta.allure</groupId>
       <artifactId>allure-cucumber7-jvm</artifactId>
       <version>${allure.version}</version>
       <scope>test</scope>
   </dependency>
   <dependency>
       <groupId>io.qameta.allure</groupId>
       <artifactId>allure-testng</artifactId>
       <version>${allure.version}</version>
       <scope>test</scope>
   </dependency>
   ```

2. Force Failsafe to include Allure listener in classpath:
   ```xml
   <!-- In ui-tests/pom.xml Failsafe configuration -->
   <additionalClasspathElements>
       <additionalClasspathElement>
           ${project.build.testOutputDirectory}
       </additionalClasspathElement>
   </additionalClasspathElements>
   ```

3. Verify Maven is downloading dependencies:
   ```bash
   mvn dependency:tree | grep allure
   ```

### Issue: `Cannot resolve allure commandline dependencies`

**Cause**: Allure Maven plugin trying to download `allure-commandline:zip` but it doesn't exist for version 2.29.1

**Solutions**:
1. **Use Allure CLI instead** (recommended):
   ```bash
   brew install allure
   allure generate target/allure-results -o target/allure-report --clean
   ```

2. **Or downgrade plugin version** :
   ```xml
   <version>2.11.2</version>  <!-- Compatible with Allure 2.29.1 -->
   ```

3. **Or skip Maven plugin and use Docker**:
   ```bash
   docker run -v $(pwd)/target:/target andrcuns/allure-docker-service generate target/allure-results
   ```

---

## Alternative: Generate Allure Report from Tests Without Maven Plugin

If Maven plugin fails, use Allure CLI directly (no Maven dependencies):

```bash
# 1. Run tests (creates target/allure-results/)
mvn -pl ui-tests -am verify

# 2. Generate report with CLI
allure generate target/allure-results -o target/allure-report --clean

# 3. Open report
open target/allure-report/index.html
```

This **only requires** Allure CLI installed via Homebrew and works independently of Maven plugin issues.

---

## What Allure Reports Show

| Feature | Description |
|---------|-------------|
| **Overview** | Total tests, pass rate, timeline |
| **Test Cases** | Individual test execution with steps |
| **Behaviors** | Test grouping by feature/story |
| **Timeline** | Test execution chronology |
| **Categories** | Test failures grouped by type |
| **History** | Test result trends over builds |
| **Flaky Tests** | Tests that intermittently fail |

---

## Integration with CI/CD

For Jenkins/GitHub Actions pipeline:

```bash
#!/bin/bash
set -e

# Install Allure if not present
command -v allure >/dev/null || brew install allure

# Run tests
mvn clean verify

# Generate Allure report
allure generate target/allure-results -o target/allure-report --clean

# Publish report URL to build output
echo "Allure Report: ${BUILD_URL}allure-report/index.html"
```

---

## Quick Reference

```bash
# Minimal working setup
brew install allure
mvn -pl ui-tests -am verify
allure generate target/allure-results -o target/allure-report --clean
open target/allure-report/index.html
```

---

**Document Version**: 1.0  
**Last Updated**: March 2026
