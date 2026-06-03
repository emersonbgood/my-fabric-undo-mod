package com.unclear.mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unclear.mod.ClearedItemsStorage;
import com.unclear.mod.UnclearMod;
import com.unclear.mod.network.UnclearNetwork;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;

/**
 * /unclear <targets>
 *
 * Restores the most recent /clear snapshot for each targeted player.
 * Requires permission level 2 (same as /clear).
 */
public class UnclearCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("unclear")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(
                                CommandManager.argument("targets", EntityArgumentType.players())
                                        .executes(ctx -> execute(
                                                ctx.getSource(),
                                                EntityArgumentType.getPlayers(ctx, "targets")
                                        ))
                        )
                        // /unclear with no args targets the command source player
                        .executes(ctx -> {
                            ServerCommandSource source = ctx.getSource();
                            ServerPlayerEntity self = source.getPlayerOrThrow();
                            return execute(source, List.of(self));
                        })
        );
    }

    private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets)
            throws CommandSyntaxException {

        int count = 0;
        for (ServerPlayerEntity player : targets) {
            List<ItemStack> items = ClearedItemsStorage.pop(player.getUuid());

            if (items.isEmpty()) {
                source.sendFeedback(
                        () -> Text.literal("§e" + player.getName().getString()
                                + " has no saved /clear history."),
                        false
                );
                continue;
            }

            int restored = 0;
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    boolean inserted = player.getInventory().insertStack(stack.copy());
                    if (!inserted) {
                        player.dropItem(stack.copy(), false);
                    }
                    restored++;
                }
            }

            final int finalRestored = restored;
            final String playerName = player.getName().getString();
            final int remaining = ClearedItemsStorage.count(player.getUuid());

            source.sendFeedback(
                    () -> Text.literal("§aRestored §f" + finalRestored
                            + "§a stack(s) to §f" + playerName
                            + (remaining > 0 ? "§a. §f" + remaining + "§a snapshot(s) still saved." : "§a.")),
                    true
            );

            if (!source.getEntity().equals(player)) {
                player.sendMessage(
                        Text.literal("§aYour items from a previous §f/clear§a were restored by §f"
                                + source.getName() + "§a."),
                        false
                );
            }

            UnclearMod.LOGGER.info("[Unclear] /unclear restored {} stack(s) to {} (executed by {})",
                    restored, playerName, source.getName());
            count++;
        }

        return count;
    }
}
