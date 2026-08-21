package com.flavomod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class WorldCreator {
    private static final Map<String, PendingAction> pendingActions = new HashMap<>();
    private static final String WORLD_PREFIX = "Flavo";

    private WorldCreator() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("fw")
                .requires(source -> source.hasPermission(2))
                .executes(context -> help(context.getSource()))
                .then(literal("list").executes(context -> list(context.getSource())))
                .then(literal("create")
                        .then(argument("type", StringArgumentType.word())
                                .then(argument("dimension", StringArgumentType.word())
                                        .then(argument("name", StringArgumentType.word())
                                                .executes(context -> create(context.getSource(),
                                                        StringArgumentType.getString(context, "type"),
                                                        StringArgumentType.getString(context, "dimension"),
                                                        StringArgumentType.getString(context, "name")))))))
                .then(literal("delete")
                        .then(argument("name", StringArgumentType.word())
                                .executes(context -> delete(context.getSource(), StringArgumentType.getString(context, "name")))))
                .then(literal("confirm").executes(context -> confirm(context.getSource()))));
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("/fw list | /fw create <Flat|Normal|Void> <Nether|Overworld|End> <name> | /fw delete <name> | /fw confirm"), false);
        return 1;
    }

    private static int create(CommandSourceStack source, String type, String dimension, String name) {
        String normalizedType = normalize(type, "Flat", "Normal", "Void");
        String normalizedDimension = normalize(dimension, "Nether", "Overworld", "End");
        String worldName = normalizeWorldName(name);
        if (normalizedType == null || normalizedDimension == null || worldName == null) {
            source.sendFailure(Component.literal("Gebruik: /fw create <Flat|Normal|Void> <Nether|Overworld|End> <name>"));
            return 0;
        }
        pendingActions.put(pendingKey(source), PendingAction.create(worldName, normalizedType, normalizedDimension));
        source.sendSuccess(() -> Component.literal("Wereld " + worldName + " klaar om aan te maken. Gebruik /fw confirm."), false);
        return 1;
    }

    private static int delete(CommandSourceStack source, String name) {
        String worldName = normalizeWorldName(name);
        if (worldName == null || !worldExists(source.getServer(), worldName)) {
            source.sendFailure(Component.literal("Wereld niet gevonden."));
            return 0;
        }
        pendingActions.put(pendingKey(source), PendingAction.delete(worldName));
        source.sendSuccess(() -> Component.literal("Verwijderen van " + worldName + " klaar om te bevestigen. Gebruik /fw confirm."), false);
        return 1;
    }

    private static int confirm(CommandSourceStack source) {
        PendingAction action = pendingActions.remove(pendingKey(source));
        if (action == null) {
            source.sendFailure(Component.literal("Er staat geen actie klaar om te bevestigen."));
            return 0;
        }
        boolean success = action.type == ActionType.CREATE
                ? createWorldFolder(source.getServer(), action.worldName, action.generator, action.dimension)
                : deleteWorldFolder(source.getServer(), action.worldName);
        source.sendSuccess(() -> Component.literal(success ? "Actie uitgevoerd voor " + action.worldName + "." : "Actie mislukt voor " + action.worldName + "."), false);
        return success ? 1 : 0;
    }

    private static int list(CommandSourceStack source) {
        List<String> worlds = readWorlds(source.getServer());
        source.sendSuccess(() -> Component.literal(worlds.isEmpty() ? "Geen Flavo-werelden gevonden." : "Flavo-werelden: " + String.join(", ", worlds)), false);
        return 1;
    }

    private static boolean createWorldFolder(MinecraftServer server, String worldName, String generator, String dimension) {
        try {
            Path folder = worldsFolder(server).resolve(worldName);
            Files.createDirectories(folder);
            Files.writeString(folder.resolve("world.properties"), "name=" + worldName + "\ntype=" + generator + "\ndimension=" + dimension + "\n");
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    private static boolean deleteWorldFolder(MinecraftServer server, String worldName) {
        try {
            Path folder = worldsFolder(server).resolve(worldName);
            if (!Files.isDirectory(folder)) {
                return false;
            }
            try (var paths = Files.walk(folder)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
            }
            return !Files.exists(folder);
        } catch (IOException exception) {
            return false;
        }
    }

    private static List<String> readWorlds(MinecraftServer server) {
        try {
            Path folder = worldsFolder(server);
            if (!Files.isDirectory(folder)) {
                return List.of();
            }
            try (var paths = Files.list(folder)) {
                return paths.filter(Files::isDirectory).map(path -> path.getFileName().toString()).sorted().toList();
            }
        } catch (IOException exception) {
            return List.of();
        }
    }

    private static Path worldsFolder(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("flavo-worlds");
    }

    private static boolean worldExists(MinecraftServer server, String worldName) {
        return Files.isDirectory(worldsFolder(server).resolve(worldName));
    }

    private static String normalize(String value, String... allowed) {
        for (String option : allowed) {
            if (option.equalsIgnoreCase(value)) {
                return option;
            }
        }
        return null;
    }

    private static String normalizeWorldName(String value) {
        String clean = value.replaceAll("[^A-Za-z0-9_-]", "");
        if (clean.isEmpty() || clean.length() > 24) {
            return null;
        }
        return clean.regionMatches(true, 0, WORLD_PREFIX, 0, WORLD_PREFIX.length()) ? clean : WORLD_PREFIX + clean;
    }

    private static String pendingKey(CommandSourceStack source) {
        return source.getTextName();
    }

    private enum ActionType { CREATE, DELETE }

    private record PendingAction(ActionType type, String worldName, String generator, String dimension) {
        private static PendingAction create(String worldName, String generator, String dimension) {
            return new PendingAction(ActionType.CREATE, worldName, generator, dimension);
        }

        private static PendingAction delete(String worldName) {
            return new PendingAction(ActionType.DELETE, worldName, null, null);
        }
    }
}