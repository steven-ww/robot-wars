Feature: Robot Battle
  As a user
  I want to create and manage robot battles
  So that I can see robots compete against each other

  Scenario: Create a new robot battle
    Given I have a valid authentication token
    When I create a new battle with the name "Epic Robot Showdown"
    Then the battle should be created successfully
    And I should receive a battle ID

  Scenario: Add robots to a battle
    Given I have a valid authentication token
    And I have a battle with ID "battle-123"
    When I add robot "R2-D2" to the battle
    And I add robot "C-3PO" to the battle
    Then the battle should have 2 robots
    And the robots should be ready for battle

  Scenario: Start a robot battle
    Given I have a valid authentication token
    And I have a battle with ID "battle-123" with 2 robots
    When I start the battle
    Then the battle status should change to "IN_PROGRESS"
    And I should receive real-time updates about the battle