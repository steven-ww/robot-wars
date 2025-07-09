Feature: Robot Hit Points
  As a robot owner
  I want my robot to have hit points configured by the server
  So that it reflects the robot's durability and health during the battles

  Background:
    Given the battle service is reset

  Scenario: Robot has server-configured hit points on creation
    Given I create a new battle with name "Hit Points Battle" and dimensions 20x20
    And I have registered my robot "DurableBot" with hit points 100
    When the battle starts
    Then the robot "DurableBot" should have 100 hit points

  Scenario: Robot hit points reduce to zero and is destroyed
    Given I create a new battle with name "Destroy Test Battle"
    And a robot "WeakBot" with hit points 100 is registered
    And the battle has started
    When "WeakBot" collides with a wall
    Then "WeakBot" hit points should reduce to zero
    And the state of "WeakBot" should be "crashed"
    And "WeakBot" should no longer participate in the battle

  Scenario: Robot with zero hit points cannot act
    Given I create a new battle with name "Zero Hit Points Battle"
    And I have registered my robot "NonParticipantBot" with hit points 100
    When the battle starts
    And "NonParticipantBot" collides with a wall
    Then "NonParticipantBot" should remain inactive

  Scenario: Battle ends with only one robot remaining
    Given I create a new battle with name "Final Stand Battle"
    And multiple robots are registered
    And the battle has started
    When all but one robot hit points reduce to zero
    Then the battle should end
    And the remaining robot should be declared the winner
    And the battle state should update to reflect the winner

