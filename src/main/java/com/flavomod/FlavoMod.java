package com.flavomod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlavoMod implements ModInitializer {
	public static final String MOD_ID = "flavomod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("FlavoMod initialized for Minecraft 1.21.1!");
		// Initialize commands here when they are created
	}
}
