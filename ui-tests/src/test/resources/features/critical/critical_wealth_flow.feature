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
