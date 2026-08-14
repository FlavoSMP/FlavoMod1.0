package com.flavomod;

import com.flavomod.commands.SidebarManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlavoMod implements ModInitializer {
    public static final String MOD_ID = "flavomod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("FlavoMod 1.21.1 initialized!");

        // 1. Register Texture Pack / Resource Pack enforcement logic
        Texturepack.register();

        // 2. Initialize sidebar when the server starts
        ServerLifecycleEvents.SERVER_STARTED.register(SidebarManager::setupSidebar);

        // 3. Refresh sidebar count on player join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            SidebarManager.updateSidebar(server);
        });

        // 4. Refresh sidebar count on player leave
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            server.execute(() -> SidebarManager.updateSidebar(server));
        });
    }
}
