Feature: Battle Creation
  As a user
  I want to create a new battle
  So that robots can participate in it

  Scenario: Create a battle with default arena size
    When I create a new battle with name "Epic Robot Showdown"
    Then a battle with the name "Epic Robot Showdown" should be created
    And the battle should have an arena with the default size from server configuration
    And the robot movement time should be the default from server configuration
    And I should receive the battle details including id, name, and arena dimensions

  Scenario: Create a battle with custom arena size
    When I create a new battle with name "Custom Arena Battle" and dimensions 20x30
    Then a battle with the name "Custom Arena Battle" should be created
    And the battle should have an arena with dimensions 20x30
    And the robot movement time should be the default from server configuration
    And I should receive the battle details including id, name, and arena dimensions

  Scenario: Create a battle with custom robot movement time
    When I create a new battle with name "Custom Movement Battle" and robot movement time 0.5 seconds
    Then a battle with the name "Custom Movement Battle" should be created
    And the battle should have an arena with the default size from server configuration
    And the robot movement time should be 0.5 seconds
    And I should receive the battle details including id, name, and arena dimensions

  Scenario: Create a battle with custom arena size and robot movement time
    When I create a new battle with name "Custom Battle" and dimensions 20x30 and robot movement time 0.5 seconds
    Then a battle with the name "Custom Battle" should be created
    And the battle should have an arena with dimensions 20x30
    And the robot movement time should be 0.5 seconds
    And I should receive the battle details including id, name, and arena dimensions

  Scenario: Create a battle with arena size below minimum
    When I attempt to create a battle with name "Too Small Arena" and dimensions 5x5
    Then I should receive an error indicating the arena size is too small
    And the minimum arena size should be 10x10

  Scenario: Create a battle with arena size above maximum
    When I attempt to create a battle with name "Too Large Arena" and dimensions 1200x1200
    Then I should receive an error indicating the arena size is too large
    And the maximum arena size should be 1000x1000
