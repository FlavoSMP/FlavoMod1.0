package com.flavomod.commands;

import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

/**
 * Help command for displaying mod information
 */
public class HelpCommand {
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		// Register help command here
	}

	private static int execute(CommandContext<ServerCommandSource> context) {
		// Add help command logic here
		return 1;
	}
}
