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
