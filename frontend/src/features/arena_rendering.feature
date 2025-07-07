Feature: Arena Rendering
  As a user
  I want to see the initial state of a battle arena
  So that I can visualize the robot positions and arena dimensions

  Background:
    Given the battle state websocket is available
    And a battle with ID "test-battle-id" exists on the server
    And the battle has an arena with dimensions 20x20
    And the battle has 2 robots registered

  Scenario: Render the initial arena state
    When I navigate to the arena page
    And I connect to the battle state websocket
    Then I should see the arena with dimensions 20x20
    And I should see 2 robots on the arena
    And each robot should be displayed at its correct position

  Scenario: Update the arena when robot positions change
    Given I am viewing the arena
    When a robot moves to a new position
    Then the robot's position on the arena should be updated

  Scenario: Display robot status
    Given I am viewing the arena
    When a robot's status changes to "MOVING"
    Then the robot should be displayed with a "MOVING" indicator

  Scenario: Handle connection errors
    When the websocket connection fails
    Then I should see an error message
    And I should have an option to reconnect