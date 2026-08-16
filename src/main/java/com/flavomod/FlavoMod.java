package com.flavomod;

import com.flavomod.commands.SidebarManager;
import com.flavomod.commands.Currency;
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

        // Initialize currency system
        Currency.initialize();

        // Sidebar updates / animation
        ServerTickEvents.END_SERVER_TICK.register(SidebarManager::tick);

        // Load player currency when they join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Currency.loadPlayer(handler.getPlayer());
        });

        // Save player currency and clean up sidebar when they leave
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            Currency.savePlayer(handler.getPlayer());
            SidebarManager.onPlayerLeave(server, handler.getPlayer());
        });
    }
}
