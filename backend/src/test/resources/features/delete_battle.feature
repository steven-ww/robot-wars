Feature: Delete Battle REST API
  As a frontend application
  I want to delete completed battles
  So that users can clean up their battle list and remove old battles

  Background:
    Given the battle service is reset

  Scenario: Successfully delete a completed battle
    Given I have created a battle with name "Completed Battle"
    And I have registered robot "Robot1" for the battle
    And I have registered robot "Robot2" for the battle
    And the battle has been completed with a winner
    When I make a DELETE request to "/api/battles/{battleId}"
    Then the response status should be 204
    And the battle should no longer exist in the system
    When I make a GET request to "/api/battles"
    Then the response should not contain the deleted battle

  Scenario: Cannot delete a battle that is not completed
    Given I have created a battle with name "Active Battle"
    And I have registered robot "Robot1" for the battle
    And I have registered robot "Robot2" for the battle
    And the battle is in progress
    When I make a DELETE request to "/api/battles/{battleId}"
    Then the response status should be 409
    And the response should contain error message "Cannot delete battle that is not completed"
    And the battle should still exist in the system

  Scenario: Cannot delete a battle in WAITING_ON_ROBOTS state
    Given I have created a battle with name "Waiting Battle"
    When I make a DELETE request to "/api/battles/{battleId}"
    Then the response status should be 409
    And the response should contain error message "Cannot delete battle that is not completed"
    And the battle should still exist in the system

  Scenario: Cannot delete a battle in READY state
    Given I have created a battle with name "Ready Battle"
    And I have registered robot "Robot1" for the battle
    And I have registered robot "Robot2" for the battle
    When I make a DELETE request to "/api/battles/{battleId}"
    Then the response status should be 409
    And the response should contain error message "Cannot delete battle that is not completed"
    And the battle should still exist in the system

  Scenario: Handle deletion of non-existent battle
    When I make a DELETE request to "/api/battles/non-existent-id"
    Then the response status should be 404
    And the response should contain error message "Battle not found"

  Scenario: Delete battle removes all associated data
    Given I have created a battle with name "Data Battle"
    And I have registered robot "Robot1" for the battle
    And I have registered robot "Robot2" for the battle
    And the battle has been completed with a winner
    And the battle has movement history and robot status data
    When I make a DELETE request to "/api/battles/{battleId}"
    Then the response status should be 204
    And all robot data for the battle should be removed
    And all battle history should be removed
    And no traces of the battle should remain in the system

  Scenario: Multiple completed battles can be deleted independently
    Given I have created a battle with name "Battle One"
    And I have created a battle with name "Battle Two"
    And both battles have been completed
    When I make a DELETE request to "/api/battles/{battleOneId}"
    Then the response status should be 204
    And "Battle One" should be deleted
    And "Battle Two" should still exist
    When I make a GET request to "/api/battles"
    Then the response should contain "Battle Two"
    And the response should not contain "Battle One"

  Scenario: API handles concurrent deletion attempts gracefully
    Given I have created a battle with name "Concurrent Battle"
    And the battle has been completed
    When I make simultaneous DELETE requests to "/api/battles/{battleId}"
    Then one request should return status 204
    And subsequent requests should return status 404
    And the battle should be deleted only once

  Scenario: Automatically delete inactive battles after 30 minutes
    Given I have created a battle with name "Inactive Battle"
    And the battle has been inactive for 30 minutes
    When I make a GET request to "/api/battles"
    Then the response should not contain "Inactive Battle"
    And the inactive battle should be automatically deleted from the system
