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
  Scenario: Accept robot move with valid parameters
    When I move a robot with direction "NORTH" and 3 blocks
    Then the robot should move successfully
    And the response status should be 200

  # Radar Scan Validation Scenarios
  Scenario: Accept radar scan with valid parameters
    When I perform radar scan with range 5
    Then the radar scan should complete successfully
    And the response status should be 200

  # Laser Fire Validation Scenarios
  Scenario: Accept laser fire with valid parameters
    When I fire laser with direction "NORTH" and range 10
    Then the laser should fire successfully
    And the response status should be 200
