Feature: Robot Registration
  As a user
  I want to add my robot to a battle
  So that I can participate in the battle when it starts

  Background:
    Given the battle service is reset

  Scenario: Register my robot as a participant in the battle
    Given I create a new battle with name "Robot Battle"
    When I register my Robot supplying it's name
    Then a robot id should be generated for my robot
    And I should receive the battle id and a unique robot id

  Scenario: Register my robot as a participant in the battle that is in progress
    Given I create a new battle with name "In Progress Battle"
    And At least two robots have registered for the battle
    And the battle administrator has started the battle
    When I register my Robot supplying it's name
    Then I should receive an error code and description reflecting that I can't join an in progress battle

  Scenario: Check the status of the battle after registering an no other robots have registered yet
    Given I create a new battle with name "Single Robot Battle"
    And I have registered my robot
    When I check the status of the battle supplying my battle id
    And it's a valid battle and robot id
    Then the battle should have 1 robots
    And the battle state should "WAITING_ON_ROBOTS"

  Scenario: Check the status of the battle and at least one other robot has registered
    Given I create a new battle with name "Multiple Robots Battle"
    And I have registered my robot
    And at least one other robot has registered
    And it's a valid battle and robot id
    When I check the status of the battle supplying my battle id and robot id
    Then the battle should have 2 or more robots
    And the battle state should "READY"

  Scenario: The battle has started when I check the status
    Given I create a new battle with name "Started Battle"
    And At least two robots have registered for the battle
    And the battle administrator has started the battle
    When I check the status of the battle supplying my battle id and robot id
    And it's a valid battle and robot id
    Then the battle status should be to "IN_PROGRESS"

  # Robot Registration Validation Scenarios
  Scenario: Reject robot registration with null name
    Given I create a new battle with name "Test Battle"
    When I attempt to register a robot with null name
    Then I should receive a validation error "Robot name is required and cannot be empty"

  Scenario: Reject robot registration with empty name
    Given I create a new battle with name "Test Battle"
    When I attempt to register a robot with empty name
    Then I should receive a validation error "Robot name is required and cannot be empty"

  Scenario: Reject robot registration with whitespace-only name
    Given I create a new battle with name "Test Battle"
    When I attempt to register a robot with whitespace-only name
    Then I should receive a validation error "Robot name is required and cannot be empty"

  Scenario: Reject robot registration with name too long
    Given I create a new battle with name "Test Battle"
    When I attempt to register a robot with name longer than 50 characters
    Then I should receive a validation error "Robot name must be 50 characters or less"

  Scenario: Reject robot registration with invalid characters in name
    Given I create a new battle with name "Test Battle"
    When I attempt to register a robot with name "Robot@Name!"
    Then I should receive a validation error "Robot name can only contain letters, numbers, spaces, hyphens, and underscores"

  Scenario: Accept robot registration with valid special characters
    Given I create a new battle with name "Test Battle"
    When I register a robot with name "Valid-Robot_Name 123"
    Then a robot id should be generated for my robot
    And I should receive the battle id and a unique robot id

  Scenario: Reject robot registration with null request body
    Given I create a new battle with name "Test Battle"
    When I attempt to register a robot with null request body
    Then I should receive a validation error "Robot data is required"

  # Battle ID Validation Scenarios
  Scenario: Reject robot registration for specific battle with null battle ID
    When I attempt to register a robot for battle with null battle ID
    Then I should receive a validation error "Battle ID is required and cannot be empty"

  Scenario: Reject robot registration for specific battle with empty battle ID
    When I attempt to register a robot for battle with empty battle ID
    Then I should receive a validation error "Battle ID is required and cannot be empty"

  Scenario: Reject robot registration for specific battle with battle ID too long
    When I attempt to register a robot for battle with battle ID longer than 100 characters
    Then I should receive a validation error "Battle ID is too long"
