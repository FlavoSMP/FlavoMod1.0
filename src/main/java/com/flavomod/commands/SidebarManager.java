package com.flavomod;

import net.minecraft.scoreboard.*;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SidebarManager {

    private static ScoreboardObjective objective;

    public static void setupSidebar(MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();

        // 1. Clear any old instance of this scoreboard to prevent duplicates
        ScoreboardObjective existing = scoreboard.getNullableObjective("flavomod_sidebar");
        if (existing != null) {
            scoreboard.removeObjective(existing);
        }

        // 2. Create the main objective with a bold title
        objective = scoreboard.addObjective(
            "flavomod_sidebar",
            ScoreboardCriterion.DUMMY,
            Text.literal("MINEHUT").formatted(Formatting.BOLD, Formatting.AQUA),
            ScoreboardCriterion.RenderType.INTEGER,
            true,
            null
        );

        // 3. Set it to display on the SIDEBAR slot
        scoreboard.setNullableObjective(ScoreboardDisplaySlot.SIDEBAR, objective);

        // 4. Populating the initial lines
        updateSidebar(server);
    }

    public static void updateSidebar(MinecraftServer server) {
        if (objective == null) return;
        ServerScoreboard scoreboard = server.getScoreboard();

        // Higher score values place the line higher on the sidebar
        setLine(scoreboard, "line_credits", "§fCredits: §a0", 7);
        setLine(scoreboard, "line_gems", "§fGems: §a0", 6);
        setLine(scoreboard, "line_players", "§fPlayers: §a" + server.getCurrentPlayerCount(), 5);
        setLine(scoreboard, "line_space1", "§1 ", 4); // Blank line for spacing
        setLine(scoreboard, "line_servers", "§fMy Servers §7(3)", 3);
        setLine(scoreboard, "line_srv1", "§7| §fFlavosmpTest: §eStarter", 2);
        setLine(scoreboard, "line_srv2", "§7| §fFlavo: §a" + server.getCurrentPlayerCount() + "/10", 1);
    }

    private static void setLine(ServerScoreboard scoreboard, String holderId, String displayText, int position) {
        ScoreHolder holder = ScoreHolder.fromName(holderId);
        ScoreboardScore score = scoreboard.getOrCreateScore(holder, objective);

        score.setScore(position);
        score.setDisplayText(Text.literal(displayText));
        
        // 1.21 Feature: Hides the default red numbers on the right side!
        score.setNumberFormat(BlankNumberFormat.INSTANCE);
    }
}
