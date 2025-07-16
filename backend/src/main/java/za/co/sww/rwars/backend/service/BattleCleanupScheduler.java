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
 * This service runs periodically to check for battles that qualify for deletion:
 * 1. Battles in WAITING_ON_ROBOTS state (never started) that are older than 30 minutes
 * 2. Battles in IN_PROGRESS state that have been running for more than 2 hours
 * These battles are automatically deleted to keep the system clean.
 */
@ApplicationScoped
public class BattleCleanupScheduler {

    private static final Logger LOGGER = Logger.getLogger(BattleCleanupScheduler.class.getName());

    // Inactivity threshold in minutes
    private static final int INACTIVITY_THRESHOLD_MINUTES = 30;

    @Inject
    private BattleService battleService;

    /**
     * Scheduled task that runs every 10 minutes to clean up battles that qualify for deletion.
     *
     * This method identifies battles that:
     * 1. Are in WAITING_ON_ROBOTS state (never started) and created more than 30 minutes ago
     * 2. Are in IN_PROGRESS state and have been running for more than 2 hours
     * 3. Automatically deletes them to prevent accumulation of abandoned or long-running battles
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
     * Determines if a battle should be deleted due to inactivity or excessive runtime.
     *
     * A battle qualifies for deletion if:
     * 1. It's in WAITING_ON_ROBOTS state and was created before the cutoff time (30 minutes ago)
     * 2. It's in IN_PROGRESS state and has been running for more than 2 hours
     *
     * @param battle The battle to check
     * @param cutoffTime The time cutoff for inactivity (30 minutes ago)
     * @return true if the battle should be deleted, false otherwise
     */
    private boolean shouldDeleteBattle(Battle battle, LocalDateTime cutoffTime) {
        // Delete battles in WAITING_ON_ROBOTS state (never started) and older than cutoff time
        if (battle.getState() == Battle.BattleState.WAITING_ON_ROBOTS
            && battle.getCreatedAt() != null
            && battle.getCreatedAt().isBefore(cutoffTime)) {
            return true;
        }

        // Delete battles in IN_PROGRESS state running for more than 2 hours
        if (battle.getState() == Battle.BattleState.IN_PROGRESS
            && battle.getCreatedAt() != null
            && battle.getCreatedAt().plusHours(2).isBefore(LocalDateTime.now())) {
            return true;
        }

        return false;
    }
}
