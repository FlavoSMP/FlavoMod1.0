package com.flavomod.commands;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Texturepack {

    private static final String PACK_URL = "https://6a7b31d3ad6ea4e2ae52ce5c.manager.minehut.com/v1/resource_packs/a22a133f-c343-45f3-a948-9bcbfe15ce61";

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            sendChatPrompt(player);
        });
    }

    private static void sendChatPrompt(ServerPlayerEntity player) {
        Text clickableLink = Text.literal("👉 [CLICK HERE TO DOWNLOAD TEXTURES] 👈")
            .styled(style -> style
                .withColor(Formatting.GOLD)
                .withBold(true)
                .withUnderline(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, PACK_URL))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to open download page in browser!")))
            );

        Text message = Text.literal("\n🔥 ").formatted(Formatting.RED)
            .append(Text.literal("FlavoMod: ").formatted(Formatting.BOLD, Formatting.WHITE))
            .append(Text.literal("To see custom items like the Fire Shard, grab the texture pack here:\n").formatted(Formatting.GRAY))
            .append(clickableLink)
            .append(Text.literal("\n"));

        player.sendMessage(message, false);
    }
}
