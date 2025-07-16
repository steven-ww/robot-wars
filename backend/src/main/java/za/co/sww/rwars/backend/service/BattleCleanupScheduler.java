package za.co.sww.rwars.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.scheduler.Scheduled;
import za.co.sww.rwars.backend.model.Battle;

/**
 * Scheduled service for automatic cleanup of inactive battles.
 *
 * This service runs periodically to check for battles that have been inactive
 * for more than 30 minutes and automatically deletes them to keep the system clean.
 */
@ApplicationScoped
public class BattleCleanupScheduler {

    private static final Logger LOGGER = Logger.getLogger(BattleCleanupScheduler.class.getName());

    // Inactivity threshold in minutes
    private static final int INACTIVITY_THRESHOLD_MINUTES = 30;

    @Inject
    private BattleService battleService;

    /**
     * Scheduled task that runs every 10 minutes to clean up inactive battles.
     *
     * This method identifies battles that:
     * 1. Are in WAITING_ON_ROBOTS state (never started)
     * 2. Were created more than 30 minutes ago
     * 3. Automatically deletes them to prevent accumulation of abandoned battles
     */
    @Scheduled(every = "10m")
    public void cleanupInactiveBattles() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(INACTIVITY_THRESHOLD_MINUTES);
            List<Battle> allBattles = battleService.getAllBattles();

            int deletedCount = 0;

            for (Battle battle : allBattles) {
                // Check if battle qualifies for automatic deletion
                if (shouldDeleteBattle(battle, cutoffTime)) {
                    try {
                        battleService.deleteInactiveBattle(battle.getId());
                        deletedCount++;
                        LOGGER.info("Automatically deleted inactive battle: " + battle.getName()
                                + " (ID: " + battle.getId() + ")");
                    } catch (Exception e) {
                        LOGGER.warning("Failed to delete inactive battle " + battle.getId() + ": " + e.getMessage());
                    }
                }
            }

            if (deletedCount > 0) {
                LOGGER.info("Cleanup completed. Deleted " + deletedCount + " inactive battles.");
            }
        } catch (Exception e) {
            LOGGER.warning("Error during battle cleanup: " + e.getMessage());
        }
    }

    /**
     * Determines if a battle should be deleted due to inactivity.
     *
     * @param battle The battle to check
     * @param cutoffTime The time cutoff for inactivity
     * @return true if the battle should be deleted, false otherwise
     */
    private boolean shouldDeleteBattle(Battle battle, LocalDateTime cutoffTime) {
        // Only delete battles that are in WAITING_ON_ROBOTS state (never started)
        if (battle.getState() != Battle.BattleState.WAITING_ON_ROBOTS) {
            return false;
        }

        // Only delete battles that were created before the cutoff time
        if (battle.getCreatedAt() == null || battle.getCreatedAt().isAfter(cutoffTime)) {
            return false;
        }

        return true;
    }
}
