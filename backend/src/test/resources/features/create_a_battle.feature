Feature: Battle Creation
  As a user
  I want to create a new battle
  So that robots can participate in it

  Background:
    Given the battle service is reset

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

  Scenario: Prevent creating battles with duplicate names
    Given I have created a battle with name "Existing Battle"
    When I attempt to create another battle with name "Existing Battle"
    Then I should receive an error indicating the battle name already exists

  # Battle Name Validation Scenarios
  Scenario: Reject battle creation with null name
    When I attempt to create a battle with null name
    Then I should receive a validation error "Battle name is required and cannot be empty"

  Scenario: Reject battle creation with empty name
    When I attempt to create a battle with empty name
    Then I should receive a validation error "Battle name is required and cannot be empty"

  Scenario: Reject battle creation with whitespace-only name
    When I attempt to create a battle with whitespace-only name
    Then I should receive a validation error "Battle name is required and cannot be empty"

  Scenario: Reject battle creation with name too long
    When I attempt to create a battle with name longer than 100 characters
    Then I should receive a validation error "Battle name must be 100 characters or less"

  Scenario: Reject battle creation with invalid characters in name
    When I attempt to create a battle with name "Battle@Name!"
    Then I should receive a validation error "Battle name can only contain letters, numbers, spaces, hyphens, and underscores"

  Scenario: Accept battle creation with valid special characters
    When I create a new battle with name "Valid-Battle_Name 123"
    Then a battle with the name "Valid-Battle_Name 123" should be created
    And I should receive the battle details including id, name, and arena dimensions

  Scenario: Reject battle creation with null request body
    When I attempt to create a battle with null request body
    Then I should receive a validation error "Battle data is required"
