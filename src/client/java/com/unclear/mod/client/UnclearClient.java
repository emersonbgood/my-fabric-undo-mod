package com.unclear.mod.client;

import com.unclear.mod.network.UnclearNetwork;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side entry point.
 * Registers the Y keybinding and sends a packet to the server when pressed.
 */
public class UnclearClient implements ClientModInitializer {

    private static KeyBinding unclearKey;

    @Override
    public void onInitializeClient() {
        // Register payload type on the client side too
        PayloadTypeRegistry.playC2S().register(
                UnclearNetwork.UnclearKeyPayload.ID,
                UnclearNetwork.UnclearKeyPayload.CODEC
        );

        unclearKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.unclear.restore",          // translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,                // default: Y
                "category.unclear"              // category shown in Controls screen
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (unclearKey.wasPressed()) {
                if (client.player != null) {
                    ClientPlayNetworking.send(new UnclearNetwork.UnclearKeyPayload());
                }
            }
        });
    }
}
