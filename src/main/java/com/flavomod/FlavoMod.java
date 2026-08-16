package com.flavomod;

import com.flavomod.commands.SidebarManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlavoMod implements ModInitializer {

    public static final String MOD_ID = "flavomod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("FlavoMod 1.21.1 initialized!");

        // Currency data is loaded and saved automatically by SavedData.
        // Sidebar updates also access it when players are online.
        ServerTickEvents.END_SERVER_TICK.register(SidebarManager::tick);
    }
}
