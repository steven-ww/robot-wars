Feature: Battle Management Validation
  As a user
  I want to manage battles with proper validation
  So that invalid requests are rejected with clear error messages

  Background:
    Given the battle service is reset

  # Start Battle Validation Scenarios
  Scenario: Reject battle start with null battle ID
    When I attempt to start a battle with null battle ID
    Then I should receive a validation error "Battle ID is required and cannot be empty"

  Scenario: Reject battle start with empty battle ID
    When I attempt to start a battle with empty battle ID
    Then I should receive a validation error "Battle ID is required and cannot be empty"

  Scenario: Reject battle start with battle ID too long
    When I attempt to start a battle with battle ID longer than 100 characters
    Then I should receive a validation error "Battle ID is too long"

  Scenario: Accept battle start with valid battle ID
    Given I create a new battle with name "Valid Battle"
    And I register a robot with name "Robot1"
    And I register a robot with name "Robot2"
    When I start the battle
    Then the battle should start successfully
    And the response status should be 200

  # Delete Battle Validation Scenarios
  Scenario: Reject battle deletion with null battle ID
    When I attempt to delete a battle with null battle ID
    Then I should receive a validation error "Battle ID is required and cannot be empty"

  Scenario: Reject battle deletion with empty battle ID
    When I attempt to delete a battle with empty battle ID
    Then I should receive a validation error "Battle ID is required and cannot be empty"

  Scenario: Reject battle deletion with battle ID too long
    When I attempt to delete a battle with battle ID longer than 100 characters
    Then I should receive a validation error "Battle ID is too long"

  Scenario: Accept battle deletion with valid battle ID
    Given I create a new battle with name "Valid Battle"
    And the battle has been completed
    When I delete the battle
    Then the battle should be deleted successfully
    And the response status should be 200
