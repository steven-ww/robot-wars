Feature: Multiple Battles Management
  As a user
  I want to create and manage multiple battles simultaneously
  So that I can organize different robot wars competitions

  Background:
    Given the battle service is reset

  Scenario: Create multiple battles with different configurations
    When I create a new battle with name "Speed Battle" and robot movement time 0.5 seconds
    And I create a new battle with name "Arena Battle" and dimensions 30x40
    And I create a new battle with name "Giant Arena" and dimensions 80x60 and robot movement time 2.0 seconds
    Then I should have 3 battles created
    And each battle should have a unique ID
    And each battle should have the correct configuration

  Scenario: Prevent creating battles with duplicate names
    Given I have created a battle with name "Unique Battle"
    When I attempt to create another battle with name "Unique Battle"
    Then I should receive an error indicating the battle name already exists

  Scenario: Register robots for specific battles
    Given I have created a battle with name "Battle One"
    And I have created a battle with name "Battle Two"
    When I register robot "Robot A" for battle "Battle One"
    And I register robot "Robot B" for battle "Battle One"
    And I register robot "Robot C" for battle "Battle Two"
    Then "Battle One" should have 2 robots
    And "Battle Two" should have 1 robot
    And each robot should be in the correct battle

  Scenario: Get all battles with their current state
    Given I have created a battle with name "Ready Battle" with 2 robots registered
    And I have created a battle with name "Empty Battle" with no robots
    And I have created a battle with name "In Progress Battle" that is currently running
    When I request all battles
    Then I should receive a list of 3 battles
    And each battle should include its name, state, robot count, and robot status
    And robot positions should not be included in the summary

  Scenario: Start battles independently
    Given I have created a battle with name "First Battle" with 2 robots registered
    And I have created a battle with name "Second Battle" with 2 robots registered
    When I start "First Battle"
    Then "First Battle" should be in "IN_PROGRESS" state
    And "Second Battle" should still be in "READY" state

  Scenario: Battles maintain independent robot states
    Given I have created a battle with name "Battle Alpha" with 2 robots registered and started
    And I have created a battle with name "Battle Beta" with 2 robots registered and started
    When I move a robot in "Battle Alpha"
    Then only robots in "Battle Alpha" should be affected
    And robots in "Battle Beta" should remain unaffected
