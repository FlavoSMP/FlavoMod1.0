package com.flavomod.commands;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class SidebarManager {

    private static int tickCounter = 0;

    // =========================================================================
    // 1. TICK LOOP: Runs every server tick to animate & refresh stats
    // =========================================================================
    public static void tick(MinecraftServer server) {
        tickCounter++;
        
        // Updates every 10 ticks (0.5 seconds) for smooth fire animations & live updates
        if (tickCounter % 10 == 0) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                updatePlayerSidebar(server, player);
            }
        }
    }

    // =========================================================================
    // 2. CLEANUP: Removes player objective when they leave to prevent memory leaks
    // =========================================================================
    public static void onPlayerLeave(MinecraftServer server, ServerPlayer player) {
        ServerScoreboard scoreboard = server.getScoreboard();
        String objName = getObjectiveName(player);
        Objective obj = scoreboard.getObjective(objName);
        if (obj != null) {
            scoreboard.removeObjective(obj);
        }
    }

    private static String getObjectiveName(ServerPlayer player) {
        String name = "sb_" + player.getScoreboardName();
        return name.length() > 16 ? name.substring(0, 16) : name;
    }

    // =========================================================================
    // 3. SIDEBAR BUILDER & REFRESHER
    // =========================================================================
    public static void updatePlayerSidebar(MinecraftServer server, ServerPlayer player) {
        ServerScoreboard scoreboard = server.getScoreboard();
        String objName = getObjectiveName(player);
        Objective objective = scoreboard.getObjective(objName);

        // Create a dedicated personal scoreboard objective for this player if it doesn't exist
        if (objective == null) {
            objective = scoreboard.addObjective(
                objName,
                ObjectiveCriteria.DUMMY,
                Component.empty(),
                ObjectiveCriteria.RenderType.INTEGER,
                true,
                null
            );
            // Force it to display ONLY for this player via a packet
            player.connection.send(new ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, objective));
        }

        // --- TITLE DESIGN: [Red Shard] Flavo SMP [Red Shard] ---
        MutableComponent redShard = Component.literal("✦").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        MutableComponent title = Component.empty()
            .append(redShard).append(" ")
            .append(getFireText("Flavo SMP", tickCounter))
            .append(" ")
            .append(redShard);
        objective.setDisplayName(title);

        // --- DATA COLLECTION (Inventory & Internal Stats) ---
        int money = countItems(player, Items.EMERALD);
        int fireShards = countItems(player, Items.AMETHYST_SHARD);
        int kills = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAYER_KILLS));
        int deaths = player.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
        int keyall = countItems(player, Items.TRIPWIRE_HOOK);
        String playtime = getFormattedPlaytime(player);

        // --- POPULATE LINES ---
        int line = 11;

        setLine(scoreboard, objective, "line_div1", "§8─────────────", line--);
        setLine(scoreboard, objective, "line_money", "🟩 §fMoney: §a" + money, line--);
        setLine(scoreboard, objective, "line_shards", "§c✦ §fFire shards: §d" + fireShards, line--);
        setLine(scoreboard, objective, "line_kills", "⚔ §fKills: §c" + kills, line--);
        setLine(scoreboard, objective, "line_deaths", "🛡 §fDeaths: §7" + deaths, line--);
        setLine(scoreboard, objective, "line_keyall", "🗝 §fKeyall: §e" + keyall, line--);
        setLine(scoreboard, objective, "line_playtime", "⏱ §fPlaytime: §b" + playtime, line--);
        setLine(scoreboard, objective, "line_div2", "§8─────────────", line--);
        
        // Static red server address title
        setLine(scoreboard, objective, "line_addr_title", "§cServer Address", line--);
        
        // Animated Server IP
        MutableComponent animatedIp = getFireText("FlavoSMP.minehut.gg", tickCounter);
        setLineComponent(scoreboard, objective, "line_addr", animatedIp, line--);
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private static void setLine(ServerScoreboard scoreboard, Objective objective, String holderId, String displayText, int position) {
        setLineComponent(scoreboard, objective, holderId, Component.literal(displayText), position);
    }

    private static void setLineComponent(ServerScoreboard scoreboard, Objective objective, String holderId, Component displayComponent, int position) {
        ScoreHolder holder = ScoreHolder.forNameOnly(holderId);
        ScoreAccess score = scoreboard.getOrCreatePlayerScore(holder, objective);
        score.set(position);
        score.display(displayComponent);
        score.setNumberFormat(BlankNumberFormat.INSTANCE); // Hides ugly numbers on the right side
    }

    // Smooth waving fire color generator
    private static MutableComponent getFireText(String text, int tick) {
        TextColor[] fireColors = {
            TextColor.fromLegacyFormat(ChatFormatting.DARK_RED),
            TextColor.fromLegacyFormat(ChatFormatting.RED),
            TextColor.fromLegacyFormat(ChatFormatting.GOLD),
            TextColor.fromLegacyFormat(ChatFormatting.YELLOW),
            TextColor.fromLegacyFormat(ChatFormatting.GOLD),
            TextColor.fromLegacyFormat(ChatFormatting.RED)
        };

        MutableComponent result = Component.empty();
        int animationFrame = tick / 4; // Animation speed

        for (int i = 0; i < text.length(); i++) {
            int colorIndex = (animationFrame + i) % fireColors.length;
            result.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(Style.EMPTY.withColor(fireColors[colorIndex]).withBold(true)));
        }
        return result;
    }

    private static int countItems(ServerPlayer player, net.minecraft.world.item.Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static String getFormattedPlaytime(ServerPlayer player) {
        int ticks = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
        int totalMins = ticks / 1200; // 1200 ticks = 1 minute
        int hours = totalMins / 60;
        int mins = totalMins % 60;
        return hours + "h " + mins + "m";
    }
}
