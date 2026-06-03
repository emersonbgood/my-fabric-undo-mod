package com.unclear.mod.network;

import com.unclear.mod.ClearedItemsStorage;
import com.unclear.mod.UnclearMod;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Handles the client → server packet sent when the player presses Y.
 */
public class UnclearNetwork {

    public static final Identifier UNCLEAR_KEY_ID = Identifier.of(UnclearMod.MOD_ID, "unclear_key");

    /**
     * Simple empty payload — the server identifies the player from the connection context.
     */
    public record UnclearKeyPayload() implements CustomPayload {
        public static final CustomPayload.Id<UnclearKeyPayload> ID =
                new CustomPayload.Id<>(UNCLEAR_KEY_ID);
        public static final PacketCodec<PacketByteBuf, UnclearKeyPayload> CODEC =
                PacketCodec.unit(new UnclearKeyPayload());

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void registerServerHandler() {
        PayloadTypeRegistry.playC2S().register(UnclearKeyPayload.ID, UnclearKeyPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UnclearKeyPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> restoreItems(player));
        });
    }

    public static void restoreItems(ServerPlayerEntity player) {
        List<ItemStack> items = ClearedItemsStorage.pop(player.getUuid());
        if (items.isEmpty()) {
            player.sendMessage(Text.literal("§eNo cleared items to restore."), false);
            return;
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

        player.sendMessage(
                Text.literal("§aRestored §f" + restored + "§a item stack(s) from your last /clear."),
                false
        );
        UnclearMod.LOGGER.info("[Unclear] Restored {} item type(s) to {} via keybind",
                restored, player.getName().getString());
    }
}
