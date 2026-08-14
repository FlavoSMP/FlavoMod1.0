package com.flavomod.commands;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class SidebarManager {

    private static Objective objective;

    public static void setupSidebar(MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();

        // 1. Clear any old instance of this scoreboard
        Objective existing = scoreboard.getObjective("flavomod_sidebar");
        if (existing != null) {
            scoreboard.removeObjective(existing);
        }

        // 2. Create the main objective with a bold title
        objective = scoreboard.addObjective(
            "flavomod_sidebar",
            ObjectiveCriteria.DUMMY,
            Component.literal("MINEHUT").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA),
            ObjectiveCriteria.RenderType.INTEGER,
            true,
            null
        );

        // 3. Set it to display on the SIDEBAR slot
        scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, objective);

        // 4. Populate initial lines
        updateSidebar(server);
    }

    public static void updateSidebar(MinecraftServer server) {
        if (objective == null) return;
        ServerScoreboard scoreboard = server.getScoreboard();

        setLine(scoreboard, "line_credits", "§fCredits: §a0", 7);
        setLine(scoreboard, "line_gems", "§fGems: §a0", 6);
        setLine(scoreboard, "line_players", "§fPlayers: §a" + server.getPlayerCount(), 5);
        setLine(scoreboard, "line_space1", "§1 ", 4);
        setLine(scoreboard, "line_servers", "§fMy Servers §7(3)", 3);
        setLine(scoreboard, "line_srv1", "§7| §fFlavosmpTest: §eStarter", 2);
        setLine(scoreboard, "line_srv2", "§7| §fFlavo: §a" + server.getPlayerCount() + "/10", 1);
    }

    private static void setLine(ServerScoreboard scoreboard, String holderId, String displayText, int position) {
        ScoreHolder holder = ScoreHolder.forNameOnly(holderId);
        ScoreAccess score = scoreboard.getOrCreatePlayerScore(holder, objective);

        score.set(position);
        score.display(Component.literal(displayText));
        
        // Hides the red numbers on the far right
        score.numberFormat(BlankFormat.INSTANCE);
    }
}
