Feature: Robot Movement
  As a user
  I want to move my robot in the battle arena
  So that I can navigate and position my robot strategically

  Scenario: Move robot in a specific direction
    Given I create a new battle with name "Movement Battle" and robot movement time 0.5 seconds
    And I have registered my robot
    And the battle has started
    When I move my robot in direction "NORTH" for 3 blocks
    Then the robot should move 3 blocks in the "NORTH" direction
    And the robot status should be "moving" during movement
    And the robot should be at the expected position after movement

  Scenario: Move robot using abbreviated directions
    Given I create a new battle with name "Abbreviated Movement Battle" and robot movement time 0.5 seconds
    And I have registered my robot
    And the battle has started
    When I move my robot in direction "NE" for 2 blocks
    Then the robot should move 2 blocks in the "NE" direction
    And the robot status should be "moving" during movement
    And the robot should be at the expected position after movement

  Scenario: Replace existing movement with new movement instruction
    Given I create a new battle with name "Replace Movement Battle" and robot movement time 0.5 seconds
    And I have registered my robot
    And the battle has started
    And I have initiated a movement in direction "SOUTH" for 5 blocks
    When I move my robot in direction "EAST" for 2 blocks before the first movement completes
    Then the robot should stop the "SOUTH" movement
    And the robot should start moving in the "EAST" direction
    And the robot should move 2 blocks in the "EAST" direction
    And the robot should be at the expected position after movement

  Scenario: Robot attempts to move beyond arena boundary
    Given I create a new battle with name "Boundary Battle" and dimensions 10x10 and robot movement time 0.5 seconds
    And I have registered my robot
    And the battle has started
    And my robot is positioned near the edge of the arena
    When I move my robot in a direction that would exceed the arena boundary
    Then the robot should move until it reaches the arena boundary
    And the robot should stop at the boundary
    And the robot status should be "crashed"

  Scenario: Get robot details via API
    Given I create a new battle with name "Robot Details Battle" and robot movement time 0.5 seconds
    And I have registered my robot
    And the battle has started
    When I request the details of my robot via the API
    Then I should receive all the information about my robot
    And the information should include the robot's ID, name, position, direction, and status

  Scenario: Verify random initial robot location
    Given I create a new battle with name "Random Location Battle" and dimensions 20x20 and robot movement time 0.5 seconds
    When I register multiple robots
    Then each robot should have a different initial position
    And all initial positions should be within the arena boundaries
