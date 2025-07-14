Feature: Arena Action Window
  As a user
  I want to see robot actions in a scrollable window next to the arena
  So that I can track what each robot is doing during the battle

  Background:
    Given the battle state websocket is available
    And a battle with ID "test-battle-id" exists on the server
    And the battle has an arena with dimensions 20x20
    And the battle has 2 robots registered

  Scenario: Show action window on the right of the arena during battle
    Given a battle is in progress
    When I navigate to the arena page for the battle
    Then I should see a window on the right of the arena
    And the window should show robot actions
    And the window should display the name of the robot for each action
    And the window should display the action taken (e.g. radar, move)

  Scenario: Action window scrolls as battle continues
    Given I am viewing the arena with the action window visible
    And the action window is displaying robot actions
    When multiple robot actions occur during the battle
    Then the action window should allow scrolling
    And new actions should be added to the bottom of the window
    And older actions should scroll up and out of view as needed

  Scenario: Action window fits within browser window for different arena sizes
    Given the arena page is rendered in a desktop browser window
    When I view an arena with dimensions <width>x<height>
    Then the entire arena should be visible without scrolling
    And the action window should be visible on the right
    And both the arena and action window should fit within the browser window

    Examples:
      | width | height |
      | 20    | 20     |
      | 50    | 50     |
      | 100   | 100    |
