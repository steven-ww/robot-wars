Feature: Battle Management
  As a user
  I want to view all battles and create new battles
  So that I can manage ongoing and new battles

  Background:
    Given the battle management API is available

  Scenario: View all existing battles on page load
    Given there are existing battles on the server
    When I navigate to the battle management page
    Then I should see a list of all battles
    And each battle should display its name and current status
    And each battle should show the number of robots registered
    And each battle should show the arena dimensions

  Scenario: View empty battle list
    Given there are no battles on the server
    When I navigate to the battle management page
    Then I should see a message indicating no battles exist
    And I should see a "Create Battle" button

  Scenario: Create a new battle with default settings
    Given I am on the battle management page
    When I click the "Create Battle" button
    And I enter "Test Battle" as the battle name
    And I click "Create" without specifying optional parameters
    Then a new battle should be created with default settings
    And the battle should appear in the battle list
    And I should see a success message

  Scenario: Create a new battle with custom arena dimensions
    Given I am on the battle management page
    When I click the "Create Battle" button
    And I enter "Custom Battle" as the battle name
    And I set arena width to 50
    And I set arena height to 50
    And I click "Create"
    Then a new battle should be created with custom dimensions
    And the battle should appear in the battle list with dimensions 50x50

  Scenario: Create a new battle with custom robot movement time
    Given I am on the battle management page
    When I click the "Create Battle" button
    And I enter "Speed Battle" as the battle name
    And I set robot movement time to 0.5 seconds
    And I click "Create"
    Then a new battle should be created with custom movement time
    And the battle should appear in the battle list

  Scenario: Create a new battle with all custom parameters
    Given I am on the battle management page
    When I click the "Create Battle" button
    And I enter "Full Custom Battle" as the battle name
    And I set arena width to 30
    And I set arena height to 40
    And I set robot movement time to 2.0 seconds
    And I click "Create"
    Then a new battle should be created with all custom parameters
    And the battle should appear in the battle list

  Scenario: Handle battle creation errors
    Given I am on the battle management page
    When I click the "Create Battle" button
    And I enter a duplicate battle name
    And I click "Create"
    Then I should see an error message
    And the battle should not be created

  Scenario: View robot details for each battle
    Given there is a battle with 2 robots registered
    When I view the battle in the battle list
    Then I should see the robot names
    And I should see each robot's current status
    And I should not see robot positions

  Scenario: Refresh battle list
    Given I am on the battle management page
    When new battles are created by other users
    And I refresh the page
    Then I should see the updated battle list
    And all new battles should be displayed

  Scenario: Render the arena for a selected battle
    Given I am on the battle management page
    When new battles are created by other users
    And I select a battle to view
    Then I should see arena for the battle rendered
