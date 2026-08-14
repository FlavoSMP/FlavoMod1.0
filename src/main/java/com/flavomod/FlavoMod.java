package com.flavomod;

import com.flavomod.commands.SidebarManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlavoMod implements ModInitializer {
    public static final String MOD_ID = "flavomod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("FlavoMod 1.21.1 initialized!");

        // 1. Register the tick loop to animate the title/IP and refresh stats every half-second
        ServerTickEvents.END_SERVER_TICK.register(SidebarManager::tick);

        // 2. Clean up player sidebar data when they disconnect to prevent memory leaks
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            SidebarManager.onPlayerLeave(server, handler.getPlayer());
        });
    }
}
