@smoke
Feature: Application smoke tests
  Quick checks to confirm the wealth management UI is up, accessible,
  and basic API endpoints respond before running deeper test suites.

  @smoke
  Scenario: Wealth management UI loads and displays the customer form
    Given I navigate to the wealth management UI
    Then the page title should contain "WealthFlow"
    And the customer creation form should be visible

  @smoke
  Scenario: Customer onboarding API endpoint is responsive
    Given I navigate to the wealth management UI
    When I submit a new customer with name "Smoke Tester" and risk "CONSERVATIVE"
    Then the customer response should contain a valid ID
