package com.flavomod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class FlavoMod implements ModInitializer {

    @Override
    public void onInitialize() {
        // Set up the scoreboard once the server finishes booting up
        ServerLifecycleEvents.SERVER_STARTED.register(SidebarManager::setupSidebar);

        // Refresh player count on join / disconnect
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            SidebarManager.updateSidebar(server);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            // Delay slightly to reflect the new count after leaving
            server.execute(() -> SidebarManager.updateSidebar(server));
        });
    }
}
