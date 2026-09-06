package com.example.commandcooldown;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Laedt und verwaltet die Cooldown-Konfiguration aus
 * config/commandcooldown.json.
 *
 * Aufbau der Datei:
 * {
 *   "defaultCooldownSeconds": 20,
 *   "commandCooldowns": {
 *     "home": 30,
 *     "tpa": 60,
 *     "spawn": 0
 *   }
 * }
 *
 * - defaultCooldownSeconds gilt fuer JEDEN Befehl, der nicht
 *   explizit in commandCooldowns aufgefuehrt ist.
 * - Ein Wert von 0 bei einem Befehl deaktiviert den Cooldown fuer
 *   genau diesen Befehl.
 * - Der Befehlsname wird ohne fuehrenden Schraegstrich angegeben
 *   (also "home", nicht "/home").
 */
public class CooldownConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("commandcooldown.json");

    public int defaultCooldownSeconds = 20;
    public Map<String, Integer> commandCooldowns = new HashMap<>();

    private static CooldownConfig instance;

    public static CooldownConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    /** Erlaubt das Neuladen der Konfiguration ohne Server-Neustart. */
    public static void reload() {
        instance = load();
    }

    private static CooldownConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                CooldownConfig cfg = GSON.fromJson(reader, CooldownConfig.class);
                if (cfg != null) {
                    if (cfg.commandCooldowns == null) {
                        cfg.commandCooldowns = new HashMap<>();
                    }
                    return cfg;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Beim allerersten Start eine Beispiel-Konfiguration erzeugen.
        CooldownConfig cfg = new CooldownConfig();
        cfg.commandCooldowns.put("home", 30);
        cfg.commandCooldowns.put("tpa", 60);
        save(cfg);
        return cfg;
    }

    private static void save(CooldownConfig cfg) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(cfg, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Liefert den Cooldown in Sekunden fuer einen bestimmten Befehlsnamen. */
    public int getCooldown(String command) {
        return commandCooldowns.getOrDefault(command.toLowerCase(), defaultCooldownSeconds);
    }
}
