package com.flavomod;

import com.flavomod.commands.SidebarManager; // <--- ADD THIS IMPORT LINE
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class FlavoMod implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(SidebarManager::setupSidebar);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            SidebarManager.updateSidebar(server);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            server.execute(() -> SidebarManager.updateSidebar(server));
        });
    }
}
