package com.example.commandcooldown.mixin;

import com.example.commandcooldown.CooldownConfig;
import com.example.commandcooldown.CooldownManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CommandDispatcher.class)
public class CommandManagerMixin {

    @Inject(
            method = "execute(Lcom/mojang/brigadier/ParseResults;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private void commandcooldown$onExecute(ParseResults<?> parseResults,
                                            CallbackInfoReturnable<Integer> cir) {
        Object rawSource = parseResults.getContext().getSource();
        if (!(rawSource instanceof CommandSourceStack source)) {
            return;
        }

        Entity entity = source.getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        List<? extends ParsedCommandNode<?>> nodes = parseResults.getContext().getNodes();
        if (nodes.isEmpty()) {
            return;
        }
        String rootCommand = nodes.get(0).getNode().getName().toLowerCase();

        int cooldownSeconds = CooldownConfig.get().getCooldown(rootCommand);
        if (cooldownSeconds <= 0) {
            return;
        }

        if (CooldownManager.isOnCooldown(player.getUUID(), rootCommand, cooldownSeconds)) {
            long remaining = CooldownManager.getRemaining(player.getUUID(), rootCommand, cooldownSeconds);
            source.sendFailure(Component.literal(
                    "Warte noch " + remaining + "s, bevor du /" + rootCommand + " erneut benutzt."));
            cir.setReturnValue(0);
            cir.cancel();
            return;
        }

        CooldownManager.setUsed(player.getUUID(), rootCommand);
    }
}
