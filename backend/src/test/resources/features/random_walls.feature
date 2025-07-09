Feature: Random Walls
  As a battle administrator
  I want to add random walls to the battle arena
  So that robots face strategic challenges and obstacles during the battle

  Background:
    Given the battle service is reset

  Scenario: Create battle with random walls
    Given I create a new battle with name "Walls Battle" and dimensions 50x50
    When the battle is created
    Then the arena should contain random walls
    And the walls should be of three types only
    And there should be squared walls of 4x4 blocks
    And there should be long walls of 1x10 blocks
    And there should be U-shaped walls of 4x10x4 blocks

  Scenario: Wall percentage does not exceed configuration
    Given I create a new battle with name "Wall Percentage Battle" and dimensions 100x100
    When the battle is created
    Then the total wall coverage should not exceed 2% of the arena space
    And the number of walls should be based on the arena size

  Scenario: Wall configuration is customizable
    Given I create a new battle with name "Custom Wall Battle" and dimensions 80x80
    And the wall configuration is set to different values
    When the battle is created
    Then the walls should reflect the custom configuration
    And the wall density should match the configured percentage

  Scenario: Robot crashes when hitting wall
    Given I create a new battle with name "Wall Crash Battle" and dimensions 30x30
    And I have registered my robot "CrashBot"
    And the battle has started
    And there are walls in the arena
    When "CrashBot" runs into a wall
    Then "CrashBot" hit points should reduce to zero
    And the state of "CrashBot" should be "crashed"
    And "CrashBot" should be unable to take further actions
