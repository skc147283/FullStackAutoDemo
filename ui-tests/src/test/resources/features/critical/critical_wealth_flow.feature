@ui @critical
Feature: Critical wealth flow from UI

  Scenario: Complete onboarding to rebalance preview from browser
    Given I open the wealth UI app
    When I onboard a balanced customer
    And I open two USD accounts with opening balances 1000 and 500
    And I deposit 250 into the source account
    And I transfer 300 from source to destination account
    And I add EQUITY holding of 12000.50
    And I request rebalance preview
    Then each workflow step should show a success response

  @critical @sanity
  Scenario: Fund transfer between accounts returns success status
    Given I open the app for a sanity test
    When I onboard a new "AGGRESSIVE" risk customer
    And I create two accounts with balances 1000 and 200
    And I transfer 300 between the accounts
    Then the transfer response should indicate success

  @critical @sanity
  Scenario: Deposit correctly updates the account balance
    Given I open the app for a sanity test
    When I onboard a new "CONSERVATIVE" risk customer
    And I open an account with opening balance 500
    And I deposit 250 into the account
    Then the deposit response should show balance 750.0
