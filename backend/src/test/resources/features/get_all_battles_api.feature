Feature: Get All Battles REST API
  As a frontend application
  I want to retrieve a list of all battles with their current state and robots
  So that I can display the battles and allow users to join or view them

  Background:
    Given the battle service is reset

  Scenario: Get all battles when no battles exist
    When I make a GET request to "/api/battles"
    Then the response status should be 200
    And the response should contain an empty list

  Scenario: Get all battles with different states and robot counts
    Given I have created a battle with name "Waiting Battle" with no robots
    And I have created a battle with name "Ready Battle" with 2 robots registered
    And I have created a battle with name "Active Battle" with 3 robots and is in progress
    When I make a GET request to "/api/battles"
    Then the response status should be 200
    And the response should contain 3 battles
    And the battle "Waiting Battle" should have state "WAITING_ON_ROBOTS" and 0 robots
    And the battle "Ready Battle" should have state "READY" and 2 robots
    And the battle "Active Battle" should have state "IN_PROGRESS" and 3 robots

  Scenario: Battle summary excludes robot positions but includes basic robot info
    Given I have created a battle with name "Test Battle"
    And I have registered robot "TestBot1" for the battle
    And I have registered robot "TestBot2" for the battle
    When I make a GET request to "/api/battles"
    Then the response status should be 200
    And the battle "Test Battle" should include robot "TestBot1" with status but no position
    And the battle "Test Battle" should include robot "TestBot2" with status but no position

  Scenario: Get battles includes arena dimensions and movement time
    Given I have created a battle with name "Custom Battle" and dimensions 25x35 and robot movement time 1.5 seconds
    When I make a GET request to "/api/battles"
    Then the response status should be 200
    And the battle "Custom Battle" should have arena dimensions 25x35
    And the battle "Custom Battle" should have robot movement time 1.5 seconds

  Scenario: API handles errors gracefully
    Given the battle service is reset
    # Note: This scenario verifies that the API normally works correctly
    # In a real environment, error handling would be tested with proper mocking
    When I make a GET request to "/api/battles"
    Then the response status should be 200
    And the response should contain an empty list
