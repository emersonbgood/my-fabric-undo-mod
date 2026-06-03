package com.unclear.mod.mixin;

import com.unclear.mod.ClearedItemsStorage;
import com.unclear.mod.UnclearMod;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.command.ClearCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Intercepts ClearCommand.execute() to snapshot items that are about to be removed.
 *
 * The vanilla method signature (1.21.4, Yarn mappings):
 *   private static int execute(ServerCommandSource source,
 *                              Collection<ServerPlayerEntity> targets,
 *                              ItemPredicate predicate,
 *                              int maxCount)
 *
 * We inject at HEAD to capture the inventory state of each target BEFORE the clear
 * happens, then store only what actually matches the predicate.
 */
@Mixin(ClearCommand.class)
public class ClearCommandMixin {

    @Inject(
            method = "execute",
            at = @At("HEAD")
    )
    private static void onClearExecute(
            net.minecraft.server.command.ServerCommandSource source,
            Collection<ServerPlayerEntity> targets,
            ItemPredicate predicate,
            int maxCount,
            CallbackInfoReturnable<Integer> cir
    ) {
        for (ServerPlayerEntity player : targets) {
            List<ItemStack> toSave = new ArrayList<>();

            // Walk every slot (including hotbar, main inv, armor, offhand)
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (stack.isEmpty()) continue;

                // Only capture stacks that match the predicate (null predicate = match all)
                if (predicate == null || predicate.test(stack)) {
                    // Respect maxCount (-1 = no limit)
                    if (maxCount == 0) {
                        // maxCount == 0 is a "count only" dry-run — don't save
                        break;
                    }
                    toSave.add(stack.copy());
                }
            }

            if (!toSave.isEmpty()) {
                ClearedItemsStorage.push(player.getUuid(), toSave);

                String executor = source.getName();
                String targetName = player.getName().getString();
                UnclearMod.LOGGER.info(
                        "[Unclear] /clear executed by {} on {} — captured {} stack type(s). " +
                        "Use /unclear {} or press Y to restore.",
                        executor, targetName, toSave.size(), targetName
                );

                // Notify the cleared player in chat
                player.sendMessage(
                        net.minecraft.text.Text.literal(
                                "§cYour inventory was cleared by §f" + executor +
                                "§c. Use §f/unclear §cor press §fY§c to restore your items."
                        ),
                        false
                );
            }
        }
    }
}
