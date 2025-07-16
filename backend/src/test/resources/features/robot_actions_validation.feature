Feature: Robot Actions Validation
  As a user
  I want to perform robot actions with proper validation
  So that invalid requests are rejected with clear error messages

  Background:
    Given the battle service is reset
    And I create a new battle with name "Action Test Battle"
    And I register a robot with name "ActionBot"
    And I register a robot with name "ActionBot2"
    And the battle administrator has started the battle

  # Move Robot Validation Scenarios
  Scenario: Reject robot move with null battle ID
    When I attempt to move a robot with null battle ID
    Then I should receive a validation error "Battle ID is required and cannot be empty"

  Scenario: Reject robot move with empty battle ID
    When I attempt to move a robot with empty battle ID
    Then I should receive a validation error "Battle ID is required and cannot be empty"

  Scenario: Reject robot move with battle ID too long
    When I attempt to move a robot with battle ID longer than 100 characters
    Then I should receive a validation error "Battle ID is too long"

  Scenario: Reject robot move with null robot ID
    When I attempt to move a robot with null robot ID
    Then I should receive a validation error "Robot ID is required and cannot be empty"

  Scenario: Reject robot move with empty robot ID
    When I attempt to move a robot with empty robot ID
    Then I should receive a validation error "Robot ID is required and cannot be empty"

  Scenario: Reject robot move with robot ID too long
    When I attempt to move a robot with robot ID longer than 100 characters
    Then I should receive a validation error "Robot ID is too long"

  Scenario: Reject robot move with null request body
    When I attempt to move a robot with null move request
    Then I should receive a validation error "Move request is required"

  Scenario: Reject robot move with null direction
    When I attempt to move a robot with null direction
    Then I should receive a validation error "Direction is required and cannot be empty"

  Scenario: Reject robot move with empty direction
    When I attempt to move a robot with empty direction
    Then I should receive a validation error "Direction is required and cannot be empty"

  Scenario: Reject robot move with invalid direction
    When I attempt to move a robot with direction "INVALID"
    Then I should receive a validation error "Invalid direction. Must be one of: NORTH, SOUTH, EAST, WEST, NE, NW, SE, SW"

  Scenario: Reject robot move with zero blocks
    When I attempt to move a robot with 0 blocks
    Then I should receive a validation error "Number of blocks must be greater than 0"

  Scenario: Reject robot move with negative blocks
    When I attempt to move a robot with -1 blocks
    Then I should receive a validation error "Number of blocks must be greater than 0"

  Scenario: Reject robot move with too many blocks
    When I attempt to move a robot with 11 blocks
    Then I should receive a validation error "Number of blocks must be 10 or less"

  Scenario: Accept robot move with valid parameters
    When I move a robot with direction "NORTH" and 3 blocks
    Then the robot should move successfully
    And the response status should be 200

  # Radar Scan Validation Scenarios
  Scenario: Reject radar scan with null battle ID
    When I attempt to perform radar scan with null battle ID
    Then I should receive a validation error "Battle ID is required and cannot be empty"

  Scenario: Reject radar scan with empty battle ID
    When I attempt to perform radar scan with empty battle ID
    Then I should receive a validation error "Battle ID is required and cannot be empty"

  Scenario: Reject radar scan with null robot ID
    When I attempt to perform radar scan with null robot ID
    Then I should receive a validation error "Robot ID is required and cannot be empty"

  Scenario: Reject radar scan with empty robot ID
    When I attempt to perform radar scan with empty robot ID
    Then I should receive a validation error "Robot ID is required and cannot be empty"

  Scenario: Reject radar scan with null request body
    When I attempt to perform radar scan with null radar request
    Then I should receive a validation error "Radar request is required"

  Scenario: Reject radar scan with zero range
    When I attempt to perform radar scan with range 0
    Then I should receive a validation error "Radar range must be greater than 0"

  Scenario: Reject radar scan with negative range
    When I attempt to perform radar scan with range -1
    Then I should receive a validation error "Radar range must be greater than 0"

  Scenario: Reject radar scan with range too large
    When I attempt to perform radar scan with range 21
    Then I should receive a validation error "Radar range must be 20 or less"

  Scenario: Accept radar scan with valid parameters
    When I perform radar scan with range 5
    Then the radar scan should complete successfully
    And the response status should be 200

  # Laser Fire Validation Scenarios
  Scenario: Reject laser fire with null battle ID
    When I attempt to fire laser with null battle ID
    Then I should receive a validation error "Battle ID is required and cannot be empty"

  Scenario: Reject laser fire with empty battle ID
    When I attempt to fire laser with empty battle ID
    Then I should receive a validation error "Battle ID is required and cannot be empty"

  Scenario: Reject laser fire with null robot ID
    When I attempt to fire laser with null robot ID
    Then I should receive a validation error "Robot ID is required and cannot be empty"

  Scenario: Reject laser fire with empty robot ID
    When I attempt to fire laser with empty robot ID
    Then I should receive a validation error "Robot ID is required and cannot be empty"

  Scenario: Reject laser fire with null request body
    When I attempt to fire laser with null laser request
    Then I should receive a validation error "Laser request is required"

  Scenario: Reject laser fire with null direction
    When I attempt to fire laser with null direction
    Then I should receive a validation error "Laser direction is required and cannot be empty"

  Scenario: Reject laser fire with empty direction
    When I attempt to fire laser with empty direction
    Then I should receive a validation error "Laser direction is required and cannot be empty"

  Scenario: Reject laser fire with invalid direction
    When I attempt to fire laser with direction "INVALID"
    Then I should receive a validation error "Invalid laser direction. Must be one of: NORTH, SOUTH, EAST, WEST, NE, NW, SE, SW"

  Scenario: Reject laser fire with zero range
    When I attempt to fire laser with range 0
    Then I should receive a validation error "Laser range must be greater than 0"

  Scenario: Reject laser fire with negative range
    When I attempt to fire laser with range -1
    Then I should receive a validation error "Laser range must be greater than 0"

  Scenario: Reject laser fire with range too large
    When I attempt to fire laser with range 51
    Then I should receive a validation error "Laser range must be 50 or less"

  Scenario: Accept laser fire with valid parameters
    When I fire laser with direction "NORTH" and range 10
    Then the laser should fire successfully
    And the response status should be 200
