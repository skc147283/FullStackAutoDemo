#!/bin/bash

##############################################################################
# Test Suite Runner: Execute 25% of Tests + Generate & Open Allure Report
# This script:
#   1. Clears target folders (maven clean)
#   2. Runs 25% of tests (1-2 API test classes + smoke UI scenarios)
#   3. Generates Allure report
#   4. Opens Allure report in default browser
##############################################################################

set -e

PROJECT_ROOT="/Users/sureshkc/Desktop/Interview/API/RestAPI_UI_DB"
cd "$PROJECT_ROOT"

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║   25% Test Suite Execution + Allure Report Generation          ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# ─────────────────────────────── STEP 1: Clean ───────────────────────────────────
echo "📦 STEP 1: Cleaning target folders..."
echo "   Running: mvn clean"
mvn clean -q
echo "   ✓ Target folders cleared"
echo ""

# ─────────────────────────────── STEP 2: Install API ──────────────────────────────
echo "🔧 STEP 2: Installing wealth-api artifact..."
echo "   Running: mvn -pl api -DskipTests install"
mvn -pl api -DskipTests install -q
echo "   ✓ Artifact installed"
echo ""

# ─────────────────────────────── STEP 3: Run 25% Tests ─────────────────────────────
echo "🧪 STEP 3: Running 25% of test suite..."
echo ""
echo "   TEST SELECTION (25% Coverage):"
echo "   ├─ API Tests (15%): AccountApiRestAssuredTest"
echo "   │  └─ Runs ~6 test methods"
echo "   └─ UI Tests (10%): Smoke scenarios (2 Cucumber scenarios)"
echo ""

echo "   Running API tests..."
mvn -pl api test \
    -Dtest=AccountApiRestAssuredTest \
    -DfailIfNoTests=false \
    -q

echo "   ✓ API tests completed"
echo ""

echo "   Running UI smoke tests..."
mvn -pl ui-tests verify \
    -Dcucumber.filter.tags="@smoke" \
    -DskipOther=true \
    -q

echo "   ✓ UI smoke tests completed"
echo ""

# ─────────────────────────────── STEP 4: Generate Allure Report ──────────────────
echo "📊 STEP 4: Generating Allure Report..."
echo "   Running: mvn allure:report"
mvn allure:report -q
echo "   ✓ Allure report generated"
echo ""

# ─────────────────────────────── STEP 5: Open Report ───────────────────────────────
echo "🌐 STEP 5: Opening Allure Report in browser..."
ALLURE_REPORT_PATH="$PROJECT_ROOT/target/site/allure-report/index.html"

if [ -f "$ALLURE_REPORT_PATH" ]; then
    echo "   Opening: file://$ALLURE_REPORT_PATH"
    open "$ALLURE_REPORT_PATH"  # macOS command to open in default browser
    echo "   ✓ Report opened successfully"
else
    echo "   ⚠ Report not found at: $ALLURE_REPORT_PATH"
    echo "   Trying alternative location..."
    ALT_PATH="$PROJECT_ROOT/target/allure-report/index.html"
    if [ -f "$ALT_PATH" ]; then
        echo "   Opening: file://$ALT_PATH"
        open "$ALT_PATH"
        echo "   ✓ Report opened successfully"
    else
        echo "   ✗ Allure report not found. Check build output above."
    fi
fi
echo ""

# ─────────────────────────────── FINAL SUMMARY ─────────────────────────────────
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                    TEST EXECUTION COMPLETE                     ║"
echo "╠════════════════════════════════════════════════════════════════╣"
echo "║  Coverage:        25% of full test suite                       ║"
echo "║  Email Pattern:   test.*.timeestamp@qa.internal                ║"
echo "║  Report Type:     Allure with detailed analytics               ║"
echo "║  Target Status:   Clean (pre-test) + Fresh Results (post-test) ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""
echo "📍 Test Report Locations:"
echo "   • Allure Report:  $ALLURE_REPORT_PATH"
echo "   • Test Results:   $PROJECT_ROOT/target/surefire-reports/"
echo "   • Test Results:   $PROJECT_ROOT/target/failsafe-reports/"
echo ""
echo "✨ Test run completed. Check browser for Allure report details."
echo ""
