package com.flavomod.commands;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.Optional;

public class SidebarManager {

    private static int tickCounter = 0;

    // =========================================================================
    // 1. TICK LOOP
    // =========================================================================

    public static void tick(MinecraftServer server) {
        tickCounter++;

        // Update every 10 ticks = 0.5 seconds
        if (tickCounter % 10 != 0) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            updatePlayerSidebar(server, player);
        }
    }

    // =========================================================================
    // 2. CLEANUP
    // =========================================================================

    public static void onPlayerLeave(MinecraftServer server, ServerPlayer player) {
        ServerScoreboard scoreboard = server.getScoreboard();

        String objectiveName = getObjectiveName(player);
        Objective objective = scoreboard.getObjective(objectiveName);

        if (objective != null) {
            // Tell this player's client to remove the objective.
            player.connection.send(
                    new ClientboundSetObjectivePacket(
                            objective,
                            1 // REMOVE
                    )
            );

            // Remove it from the server scoreboard too.
            scoreboard.removeObjective(objective);
        }
    }

    // =========================================================================
    // 3. OBJECTIVE NAME
    // =========================================================================

    private static String getObjectiveName(ServerPlayer player) {
        String name = "sb_" + player.getScoreboardName();

        // Minecraft objective names have a 16-character limit.
        return name.length() > 16
                ? name.substring(0, 16)
                : name;
    }

    // =========================================================================
    // 4. MAIN SIDEBAR UPDATE
    // =========================================================================

    public static void updatePlayerSidebar(
            MinecraftServer server,
            ServerPlayer player
    ) {
        ServerScoreboard scoreboard = server.getScoreboard();

        String objectiveName = getObjectiveName(player);

        Objective objective = scoreboard.getObjective(objectiveName);

        // ---------------------------------------------------------------------
        // CREATE OBJECTIVE
        // ---------------------------------------------------------------------

        if (objective == null) {
            objective = scoreboard.addObjective(
                    objectiveName,
                    ObjectiveCriteria.DUMMY,
                    Component.empty(),
                    ObjectiveCriteria.RenderType.INTEGER,
                    true,
                    null
            );

            // Send the objective to THIS player.
            player.connection.send(
                    new ClientboundSetObjectivePacket(
                            objective,
                            0 // ADD
                    )
            );

            // Put it in the sidebar for THIS player.
            player.connection.send(
                    new ClientboundSetDisplayObjectivePacket(
                            DisplaySlot.SIDEBAR,
                            objective
                    )
            );
        }

        // =========================================================================
        // TITLE
        // =========================================================================

        MutableComponent redShard =
                Component.literal("✦")
                        .withStyle(
                                ChatFormatting.RED,
                                ChatFormatting.BOLD
                        );

        MutableComponent title =
                Component.empty()
                        .append(redShard)
                        .append(" ")
                        .append(getFireText("Flavo SMP", tickCounter))
                        .append(" ")
                        .append(redShard);

        objective.setDisplayName(title);

        // Send the changed title to THIS player.
        player.connection.send(
                new ClientboundSetObjectivePacket(
                        objective,
                        2 // CHANGE / UPDATE
                )
        );

        // =========================================================================
        // PLAYER DATA
        // =========================================================================

        int money = countItems(
                player,
                Items.EMERALD
        );

        int fireShards = countItems(
                player,
                Items.AMETHYST_SHARD
        );

        int kills = player.getStats()
                .getValue(
                        Stats.CUSTOM.get(
                                Stats.PLAYER_KILLS
                        )
                );

        int deaths = player.getStats()
                .getValue(
                        Stats.CUSTOM.get(
                                Stats.DEATHS
                        )
                );

        int keyall = countItems(
                player,
                Items.TRIPWIRE_HOOK
        );

        String playtime = getFormattedPlaytime(player);

        // =========================================================================
        // SIDEBAR LINES
        // =========================================================================

        int line = 11;

        sendLine(
                player,
                objective,
                "line_div1",
                "§8─────────────",
                line--
        );

        sendLine(
                player,
                objective,
                "line_money",
                "🟩 §fMoney: §a" + money,
                line--
        );

        sendLine(
                player,
                objective,
                "line_shards",
                "§c✦ §fFire shards: §d" + fireShards,
                line--
        );

        sendLine(
                player,
                objective,
                "line_kills",
                "⚔ §fKills: §c" + kills,
                line--
        );

        sendLine(
                player,
                objective,
                "line_deaths",
                "🛡 §fDeaths: §7" + deaths,
                line--
        );

        sendLine(
                player,
                objective,
                "line_keyall",
                "🗝 §fKeyall: §e" + keyall,
                line--
        );

        sendLine(
                player,
                objective,
                "line_playtime",
                "⏱ §fPlaytime: §b" + playtime,
                line--
        );

        sendLine(
                player,
                objective,
                "line_div2",
                "§8─────────────",
                line--
        );

        sendLine(
                player,
                objective,
                "line_addr_title",
                "§cServer Address",
                line--
        );

        MutableComponent animatedIp =
                getFireText(
                        "FlavoSMP.minehut.gg",
                        tickCounter
                );

        sendLineComponent(
                player,
                objective,
                "line_addr",
                animatedIp,
                line--
        );
    }

    // =========================================================================
    // 5. SEND A SCOREBOARD LINE DIRECTLY TO PLAYER
    // =========================================================================

    private static void sendLine(
            ServerPlayer player,
            Objective objective,
            String holderId,
            String displayText,
            int position
    ) {
        sendLineComponent(
                player,
                objective,
                holderId,
                Component.literal(displayText),
                position
        );
    }

    private static void sendLineComponent(
            ServerPlayer player,
            Objective objective,
            String holderId,
            Component displayComponent,
            int position
    ) {
        player.connection.send(
                new ClientboundSetScorePacket(
                        holderId,
                        objective.getName(),
                        position,
                        Optional.of(displayComponent),
                        Optional.of(BlankFormat.INSTANCE)
                )
        );
    }

    // =========================================================================
    // 6. FIRE ANIMATION
    // =========================================================================

    private static MutableComponent getFireText(
            String text,
            int tick
    ) {
        TextColor[] fireColors = {
                TextColor.fromLegacyFormat(
                        ChatFormatting.DARK_RED
                ),

                TextColor.fromLegacyFormat(
                        ChatFormatting.RED
                ),

                TextColor.fromLegacyFormat(
                        ChatFormatting.GOLD
                ),

                TextColor.fromLegacyFormat(
                        ChatFormatting.YELLOW
                ),

                TextColor.fromLegacyFormat(
                        ChatFormatting.GOLD
                ),

                TextColor.fromLegacyFormat(
                        ChatFormatting.RED
                )
        };

        MutableComponent result =
                Component.empty();

        int animationFrame = tick / 4;

        for (int i = 0; i < text.length(); i++) {

            int colorIndex =
                    (animationFrame + i)
                            % fireColors.length;

            result.append(
                    Component.literal(
                            String.valueOf(
                                    text.charAt(i)
                            )
                    ).withStyle(
                            Style.EMPTY
                                    .withColor(
                                            fireColors[colorIndex]
                                    )
                                    .withBold(true)
                    )
            );
        }

        return result;
    }

    // =========================================================================
    // 7. COUNT ITEMS
    // =========================================================================

    private static int countItems(
            ServerPlayer player,
            Item item
    ) {
        int count = 0;

        for (
                int i = 0;
                i < player.getInventory().getContainerSize();
                i++
        ) {
            ItemStack stack =
                    player.getInventory().getItem(i);

            if (stack.is(item)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    // =========================================================================
    // 8. PLAYTIME
    // =========================================================================

    private static String getFormattedPlaytime(
            ServerPlayer player
    ) {
        int ticks =
                player.getStats()
                        .getValue(
                                Stats.CUSTOM.get(
                                        Stats.PLAY_TIME
                                )
                        );

        // 1200 Minecraft ticks = 1 minute
        int totalMinutes = ticks / 1200;

        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        return hours + "h " + minutes + "m";
    }
}
