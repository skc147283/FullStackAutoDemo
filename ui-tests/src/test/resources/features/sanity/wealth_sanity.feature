@sanity
Feature: Wealth management sanity tests
  Focused checks that individual business operations produce correct,
  observable outcomes – more detailed than smoke but narrower than critical flow.

  @sanity
  Scenario: Portfolio holding stores the correct asset symbol
    Given I open the app for a sanity test
    When I onboard a new "BALANCED" risk customer
    And I open an account with opening balance 1000
    And I add a holding with symbol "BONDS" and market value 3000.0
    Then the holding response should contain symbol "BONDS"
