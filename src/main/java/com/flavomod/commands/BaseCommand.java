package com.flavomod.commands;

import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.CommandDispatcher;

/**
 * Command base class - extend this to create new commands
 * Register new commands in FlavoMod.java during mod initialization
 */
public abstract class BaseCommand {
	
	public abstract void register(CommandDispatcher<ServerCommandSource> dispatcher);
}
