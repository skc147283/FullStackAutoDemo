@sanity
Feature: Wealth management sanity tests
  Focused checks that individual business operations produce correct,
  observable outcomes – more detailed than smoke but narrower than critical flow.

  @sanity
  Scenario: Deposit correctly updates the account balance
    Given I open the app for a sanity test
    When I onboard a new "CONSERVATIVE" risk customer
    And I open an account with opening balance 500
    And I deposit 250 into the account
    Then the deposit response should show balance 750.0

  @sanity
  Scenario: Portfolio holding stores the correct asset symbol
    Given I open the app for a sanity test
    When I onboard a new "BALANCED" risk customer
    And I open an account with opening balance 1000
    And I add a holding with symbol "BONDS" and market value 3000.0
    Then the holding response should contain symbol "BONDS"

  @sanity
  Scenario: Fund transfer between accounts returns success status
    Given I open the app for a sanity test
    When I onboard a new "GROWTH" risk customer
    And I create two accounts with balances 1000 and 200
    And I transfer 300 between the accounts
    Then the transfer response should indicate success
