Feature: Battle State WebSocket
  As a Front End instance
  I want to be able to determine the state of a battle on the server
  So that I can render the initial arena state

  Background:
    Given a battle with name "WebSocket Test Battle" and dimensions 20x20 exists
    And a robot named "TestBot1" is registered for the battle
    And a robot named "TestBot2" is registered for the battle

  Scenario: Connect to battle state websocket
    When I connect to the battle state websocket
    Then I should receive the battle state information
    And the battle state should include the arena dimensions 20x20
    And the battle state should include 2 registered robots
    And the battle state should include the robot "TestBot1"
    And the battle state should include the robot "TestBot2"

  Scenario: Request battle state update
    Given I am connected to the battle state websocket
    When I send an "update" message to the websocket
    Then I should receive the updated battle state information
    And the battle state should include the arena dimensions 20x20
    And the battle state should include 2 registered robots

  Scenario: Receive battle state after robot movement
    Given I am connected to the battle state websocket
    And the battle is started
    When robot "TestBot1" moves "NORTH" for 2 blocks
    And I send an "update" message to the websocket
    Then I should receive the updated battle state information
    And the battle state should include the robot "TestBot1" with status "MOVING"

  Scenario: Receive battle state after battle starts
    Given I am connected to the battle state websocket
    When the battle is started
    And I send an "update" message to the websocket
    Then I should receive the updated battle state information
    And the battle state should include battle state "IN_PROGRESS"

  Scenario: Automatic broadcast when robot starts moving
    Given I am connected to the battle state websocket
    And the battle is started
    When robot "TestBot1" moves "NORTH" for 3 blocks
    Then I should automatically receive a battle state update
    And the battle state should include the robot "TestBot1" with status "MOVING"
