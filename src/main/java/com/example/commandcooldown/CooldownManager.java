package com.example.commandcooldown;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Merkt sich pro Spieler und Befehl, wann dieser zuletzt benutzt wurde.
 * Rein im Arbeitsspeicher - nach einem Serverneustart sind alle
 * Cooldowns automatisch wieder abgelaufen.
 */
public class CooldownManager {
    private static final Map<UUID, Map<String, Long>> lastUse = new ConcurrentHashMap<>();

    public static boolean isOnCooldown(UUID playerId, String command, int cooldownSeconds) {
        return getRemaining(playerId, command, cooldownSeconds) > 0;
    }

    public static long getRemaining(UUID playerId, String command, int cooldownSeconds) {
        Map<String, Long> commands = lastUse.get(playerId);
        if (commands == null) {
            return 0;
        }
        Long last = commands.get(command);
        if (last == null) {
            return 0;
        }
        long elapsedSeconds = (System.currentTimeMillis() - last) / 1000L;
        return Math.max(0, cooldownSeconds - elapsedSeconds);
    }

    public static void setUsed(UUID playerId, String command) {
        lastUse.computeIfAbsent(playerId, key -> new ConcurrentHashMap<>())
                .put(command, System.currentTimeMillis());
    }
}
