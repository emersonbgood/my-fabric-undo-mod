package com.unclear.mod;

import com.unclear.mod.command.UnclearCommand;
import com.unclear.mod.network.UnclearNetwork;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnclearMod implements ModInitializer {

    public static final String MOD_ID = "unclear";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Unclear mod initialized — /clear interception active.");

        UnclearNetwork.registerServerHandler();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                UnclearCommand.register(dispatcher)
        );
    }
}
