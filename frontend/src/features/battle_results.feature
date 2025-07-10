Feature: Battle Results Display
  As a user watching a battle
  I want to see the results when the battle completes
  So that I know which robot won

  Scenario: Display battle results when battle is completed with a winner
    Given a battle arena is displayed for a completed battle
    When the battle state shows COMPLETED with a winner
    Then the battle results should be displayed
    And the winner should be announced
    And the battle summary should show final statistics
    And the final robot status should be displayed
    And the winner robot should be highlighted on the arena

  Scenario: Display battle results when battle is completed without a clear winner
    Given a battle arena is displayed for a completed battle
    When the battle state shows COMPLETED without a winner
    Then the battle results should be displayed
    And a no winner message should be shown

  Scenario: Battle state badge shows correct status
    Given a battle arena is displayed
    When the battle state is IN_PROGRESS
    Then the battle state badge should show IN_PROGRESS
    When the battle state changes to COMPLETED
    Then the battle state badge should show COMPLETED
