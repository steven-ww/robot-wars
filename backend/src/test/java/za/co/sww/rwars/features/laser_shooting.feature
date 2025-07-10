Feature: Laser Shooting
  In order to simulate a battle arena
  Users need to see lasers being fired and their effects

  Scenario: Fire a laser and show its path and hit
    Given a robot is positioned on the arena
    When the robot fires a laser in a direction
    Then the laser path is displayed on the UI
    And if the laser hits a robot, it shows the hit on the UI
    And the hit robot's hit points are reduced
