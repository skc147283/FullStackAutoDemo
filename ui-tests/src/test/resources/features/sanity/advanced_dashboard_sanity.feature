@sanity
Feature: Advanced dashboard sanity checks

  @sanity
  Scenario: Tab navigation works on advanced dashboard
    Given I open the advanced dashboard
    When I switch to advanced tab "Reports" with id "reports"
    Then advanced tab with id "reports" should be active
    When I switch to advanced tab "Upload Data" with id "fileupload"
    Then advanced tab with id "fileupload" should be active

  @sanity
  Scenario: Custom report generation shows success alert
    Given I open the advanced dashboard
    When I generate advanced report type "performance" for range "quarter"
    Then advanced report success alert should be displayed

  @sanity
  Scenario: CSV upload interaction works on advanced dashboard
    Given I open the advanced dashboard
    When I upload advanced csv fixture "testdata/sample_upload.csv"
    Then advanced upload should show file details and success

  @sanity
  Scenario: Quick deposit action shows success alert
    Given I open the advanced dashboard
    When I perform quick deposit of "150" from advanced dashboard
    Then advanced deposit success alert should be displayed
