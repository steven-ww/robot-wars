Feature: Robot Battle

  Scenario: Create a battle and move robots until one crashes or time limit is reached
    Given the backend service is running
    When I create a new battle
    And I register a robot with the name "Restro"
    And I register a robot with the name "ReqBot"
    And I start the battle
    Then I should be able to move the robots around the arena until one crashes into a wall or 5 minutes has passed

  Scenario: Create a battle and move robots until the time limit is reached
    Given the backend service is running
    When I create a new battle
    And I register a robot with the name "Restro"
    And I register a robot with the name "ReqBot"
    And I start the battle
    Then I should be able to fire a laser before moving the robots around the arena until a specified time has passed

  Scenario: Track robot movement to confirm it is moving as expected
    Given the backend service is running
    When I create a new battle
    And I register a robot with the name "TrackBot"
    And I start the battle
    And I move the robot in direction "NORTH" for 2 blocks
    And I fire a laser in direction "NORTH"
    Then I should be able to track the robot's position as it moves

  Scenario: Fire laser before each movement
    Given the backend service is running
    When I create a new battle
    And I register a robot with the name "LaserBot"
    And I start the battle
    And I fire a laser in direction "EAST" before moving the robot in direction "EAST" for 1 block
    Then the robot should fire a laser first and then move in the specified direction
