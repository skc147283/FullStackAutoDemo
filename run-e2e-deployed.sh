#!/usr/bin/env bash
set -euo pipefail

# Usage:
# ./run-e2e-deployed.sh https://wealth-api-demo.onrender.com

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <base-url> [suite]"
  echo "suite: smoke|sanity|critical|all (default: smoke)"
  exit 1
fi

BASE_URL="$1"
SUITE="${2:-smoke}"

case "$SUITE" in
  smoke)
    IT_TESTS="SmokeUiIT"
    ;;
  sanity)
    IT_TESTS="SanityUiIT"
    ;;
  critical)
    IT_TESTS="CriticalUiIT,CriticalBusinessUiNgIT"
    ;;
  all)
    IT_TESTS="SmokeUiIT,SanityUiIT,CriticalUiIT,CriticalBusinessUiNgIT"
    ;;
  *)
    echo "Unsupported suite: $SUITE"
    echo "Allowed: smoke|sanity|critical|all"
    exit 1
    ;;
esac

echo "Running deployed E2E suite '$SUITE' against: $BASE_URL"
mvn -pl ui-tests verify \
  -Dit.test="$IT_TESTS" \
  -Dui.headless=true \
  -Dui.base-url="$BASE_URL"

echo "Generating Allure report"
mvn -pl ui-tests allure:report

echo "Report: ui-tests/target/site/allure-maven-plugin/index.html"
