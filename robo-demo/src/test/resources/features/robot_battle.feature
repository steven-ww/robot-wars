Feature: Robot Battle

  Scenario: Create a battle and move robots until one crashes or time limit is reached
    Given the backend service is running
    When I create a new battle
    And I register a robot with the name "Restro"
    And I register a robot with the name "ReqBot"
    And I start the battle
    Then I should be able to move the robots around the arena until one crashes into a wall or 5 minutes has passed