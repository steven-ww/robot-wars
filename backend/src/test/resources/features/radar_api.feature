Feature: Radar API
  As a robot
  I want to scan my surroundings
  So that I can detect obstacles and other robots within a configurable distance

  Background:
    Given the battle service is reset

  Scenario: Robot scans area and detects walls within range
    Given I create a new battle with name "Radar Test Battle" and dimensions 20x20
    And I have registered my robot "RadarBot"
    And the battle has started
    And there are walls placed in the arena
    When I invoke the radar API with range 15
    Then I should receive a radar response showing detected walls within range
    And the response should indicate "WALL" at the detected positions

  Scenario: Robot scans area and detects other robots within range
    Given I create a new battle with name "Robot Detection Battle" and dimensions 20x20
    And I have registered my robot "Scanner"
    And I have registered another robot "Target"
    And the battle has started
    When I invoke the radar API with range 15
    Then I should receive a radar response showing detected robots within range
    And the response should indicate "ROBOT" at the detected positions

  Scenario: Robot scans area with configurable range
    Given I create a new battle with name "Range Test Battle" and dimensions 30x30
    And I have registered my robot "RangeBot"
    And the battle has started
    When I invoke the radar API with range 3
    Then I should receive a radar response for positions within 3 blocks
    And positions beyond 3 blocks should not be included in the response

  Scenario: Robot scans empty area
    Given I create a new battle with name "Empty Scan Battle" and dimensions 30x30
    And I have registered my robot "EmptyBot"
    And the battle has started
    When I invoke the radar API with range 3
    Then I should receive an empty radar response
    And no obstacles should be detected within the scanned area
