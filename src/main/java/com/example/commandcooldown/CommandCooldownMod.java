package com.example.commandcooldown;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class CommandCooldownMod implements ModInitializer {
    public static final String MOD_ID = "commandcooldown";

    @Override
    public void onInitialize() {
        CooldownConfig.get();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("cooldownconfig")
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("reload")
                                .executes(context -> {
                                    CooldownConfig.reload();
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Cooldown-Konfiguration wurde neu geladen."),
                                            false);
                                    return 1;
                                }))));
    }
}
