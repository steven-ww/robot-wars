Feature: Main App Battle Arena Tab
  As a user
  I want the Battle Arena tab to show a specific battle
  So that I can view the arena for a selected or default battle

  Background:
    Given the battle management API is available

  Scenario: Battle Arena tab shows message when no battle is selected
    Given there are existing battles on the server
    And no battle has been selected yet
    When I navigate to the Battle Arena tab
    Then I should see a message indicating no battle is selected
    And I should see a "Select Battle" button
    And I should not see any arena visualization

  Scenario: Battle Arena tab shows the last selected battle
    Given there are existing battles on the server
    And I have previously selected a battle with ID "battle-123"
    When I navigate to the Battle Arena tab
    Then I should see the arena for battle "battle-123"
    And I should see the battle name displayed
    And I should see a "Change Battle" button

  Scenario: Battle Arena tab shows default battle when available
    Given there is exactly one battle on the server with ID "default-battle"
    And no battle has been previously selected
    When I navigate to the Battle Arena tab
    Then I should see the arena for battle "default-battle"
    And I should see the battle name displayed
    And I should see a "Change Battle" button

  Scenario: Select a battle from the Battle Arena tab
    Given there are multiple battles on the server
    And I am on the Battle Arena tab with no battle selected
    When I click the "Select Battle" button
    Then I should see a list of available battles
    And I can select a battle to view its arena
    And the selected battle should be remembered for future visits to the tab

  Scenario: Change battle from the Battle Arena tab
    Given I am viewing a battle arena for battle "current-battle"
    And there are other battles available on the server
    When I click the "Change Battle" button
    Then I should see a list of available battles
    And I can select a different battle to view its arena
    And the newly selected battle should be remembered for future visits to the tab

  Scenario: Battle Arena tab handles invalid battle ID gracefully
    Given I have previously selected a battle with ID "deleted-battle"
    And the battle "deleted-battle" no longer exists on the server
    When I navigate to the Battle Arena tab
    Then I should see a message indicating the battle is no longer available
    And I should see a "Select Battle" button
    And I should not see any arena visualization
