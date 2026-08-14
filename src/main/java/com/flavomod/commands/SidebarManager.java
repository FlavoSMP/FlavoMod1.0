// =========================================================================
    // LOGIC HELPERS
    // =========================================================================

    private static String getObjectiveName(ServerPlayer player) {
        String name = "sb_" + player.getScoreboardName();
        return name.length() > 16 ? name.substring(0, 16) : name;
    }

    private static void setLine(ServerScoreboard scoreboard, Objective objective, String holderId, String displayText, int position) {
        setLineComponent(scoreboard, objective, holderId, Component.literal(displayText), position);
    }

    private static void setLineComponent(ServerScoreboard scoreboard, Objective objective, String holderId, Component displayComponent, int position) {
        ScoreHolder holder = ScoreHolder.forNameOnly(holderId);
        ScoreAccess score = scoreboard.getOrCreatePlayerScore(holder, objective);
        score.set(position);
        score.display(displayComponent);
        
        // This hides the ugly red numbers on the right side of the sidebar! (1.20.3+ feature)
        score.numberFormat(BlankFormat.INSTANCE);
    }

    // Generates a smooth, waving fire color effect loop
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
        int animationFrame = tick / 5; // Slows down the loop so it's smooth, not flashing
        
        for (int i = 0; i < text.length(); i++) {
            int colorIndex = (animationFrame + i) % fireColors.length;
            result.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(Style.EMPTY.withColor(fireColors[colorIndex]).withBold(true)));
        }
        return result;
    }

    // =========================================================================
    // DATA METHODS: Now pulling from Vanilla Scoreboards!
    // =========================================================================
    
    // Helper to grab a score from a vanilla objective
    private static int getVanillaScore(ServerScoreboard scoreboard, ServerPlayer player, String objectiveName) {
        Objective obj = scoreboard.getObjective(objectiveName);
        if (obj != null) {
            return scoreboard.getOrCreatePlayerScore(player, obj).get();
        }
        return 0; // If the objective doesn't exist yet, just show 0
    }

    private static int getMoney(ServerPlayer player) { 
        return getVanillaScore(player.server.getScoreboard(), player, "money"); 
    }
    
    private static int getFireShards(ServerPlayer player) { 
        return getVanillaScore(player.server.getScoreboard(), player, "shards"); 
    }
    
    private static int getKills(ServerPlayer player) { 
        return getVanillaScore(player.server.getScoreboard(), player, "kills"); 
    }
    
    private static int getDeaths(ServerPlayer player) { 
        return getVanillaScore(player.server.getScoreboard(), player, "deaths"); 
    }
    
    private static int getKeyall(ServerPlayer player) { 
        return getVanillaScore(player.server.getScoreboard(), player, "keyall"); 
    }
    
    // Playtime is usually tracked in ticks/minutes, you can customize this conversion
    private static String getPlaytime(ServerPlayer player) { 
        int minutes = getVanillaScore(player.server.getScoreboard(), player, "playtime");
        int hours = minutes / 60;
        int remainingMins = minutes % 60;
        return hours + "h " + remainingMins + "m"; 
    }
