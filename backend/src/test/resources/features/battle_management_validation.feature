Feature: Battle Management Validation
  As a user
  I want to manage battles with proper validation
  So that invalid requests are rejected with clear error messages

  Background:
    Given the battle service is reset

  # Start Battle Validation Scenarios
  Scenario: Accept battle start with valid battle ID
    Given I create a new battle with name "Valid Battle"
    And I register a robot with name "Robot1"
    And I register a robot with name "Robot2"
    When I start the battle
    Then the battle should start successfully
    And the response status should be 200

  # Delete Battle Validation Scenarios
  Scenario: Accept battle deletion with valid battle ID
    Given I create a new battle with name "Valid Battle"
    And the battle has been completed
    When I delete the battle
    Then the battle should be deleted successfully
    And the response status should be 204
