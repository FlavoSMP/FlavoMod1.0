package com.flavomod.commands;

import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

/**
 * Example command class for FlavoMod
 * Add your command logic here
 */
public class TestCommand {
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		// Register your commands here
	}

	private static int execute(CommandContext<ServerCommandSource> context) {
		// Add command execution logic here
		return 1;
	}
}
