package com.flavomod;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.net.URI;
import java.util.*;

public class Texturepack {

    // Your Minehut direct resource pack URL
    private static final String PACK_URL = "https://6a7b31d3ad6ea4e2ae52ce5c.manager.minehut.com/v1/resource_packs/a22a133f-c343-45f3-a948-9bcbfe15ce61";
    private static final UUID PACK_UUID = UUID.nameUUIDFromBytes(PACK_URL.getBytes());

    // Tracks players who have successfully applied the texture pack
    private static final Set<UUID> LOADED_PLAYERS = new HashSet<>();
    
    // Timer counter for the 10-second loop (20 ticks = 1 second -> 200 ticks = 10 seconds)
    private static int tickCounter = 0;

    public static void register() {
        // 1. Send prompt and chat link when player joins
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            sendChatPrompt(player);
            sendPackPacket(player);
        });

        // 2. Remove player from track list on disconnect
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            LOADED_PLAYERS.remove(handler.getPlayer().getUuid());
        });

        // 3. Track packet responses from the player (Accept/Decline/Success)
        ServerPlayNetworking.registerGlobalReceiver(ResourcePackStatusC2SPacket.PACKET_ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            ResourcePackStatusC2SPacket.Status status = payload.status();

            context.server().execute(() -> {
                switch (status) {
                    case ACCEPTED:
                        player.sendMessage(Text.literal("⬇️ Downloading FlavoMod textures...").formatted(Formatting.GREEN), false);
                        break;
                    case SUCCESSFUL:
                        LOADED_PLAYERS.add(player.getUuid());
                        player.sendMessage(Text.literal("🔥 FlavoMod textures applied successfully! Enjoy the Fire Shards!").formatted(Formatting.GOLD), false);
                        break;
                    case DECLINED:
                    case FAILED_DOWNLOAD:
                        LOADED_PLAYERS.remove(player.getUuid());
                        kickPlayer(player);
                        break;
                    default:
                        break;
                }
            });
        });

        // 4. Check every 10 seconds (200 ticks) if online players have the pack active
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter >= 200) { 
                tickCounter = 0;

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if (!LOADED_PLAYERS.contains(player.getUuid())) {
                        kickPlayer(player);
                    }
                }
            }
        });
    }

    /**
     * Sends the formatted chat message with a clickable URL fallback.
     */
    private static void sendChatPrompt(ServerPlayerEntity player) {
        Text clickableLink = Text.literal("👉 [CLICK HERE TO MANUAL DOWNLOAD] 👈")
            .styled(style -> style
                .withColor(Formatting.GOLD)
                .withBold(true)
                .withUnderline(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, PACK_URL))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to open download page in browser!")))
            );

        Text message = Text.literal("\n🔥 ").formatted(Formatting.RED)
            .append(Text.literal("FlavoMod: ").formatted(Formatting.BOLD, Formatting.WHITE))
            .append(Text.literal("To see custom items like the Fire Shard, accept the prompt or use the link:\n").formatted(Formatting.GRAY))
            .append(clickableLink)
            .append(Text.literal("\n"));

        player.sendMessage(message, false);
    }

    /**
     * Sends the native Minecraft resource pack request packet.
     */
    private static void sendPackPacket(ServerPlayerEntity player) {
        try {
            Text promptMessage = Text.literal("FlavoMod requires the custom texture pack to display Fire Shards properly!");
            
            ResourcePackSendS2CPacket packet = new ResourcePackSendS2CPacket(
                PACK_UUID,
                URI.create(PACK_URL),
                "", 
                true, // Required = true (forces kick if declined)
                Optional.of(promptMessage)
            );

            player.networkHandler.sendPacket(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Kicks the player if they haven't loaded the required textures.
     */
    private static void kickPlayer(ServerPlayerEntity player) {
        player.networkHandler.disconnect(
            Text.literal("❌ You were disconnected because the FlavoMod texture pack is not enabled.\n\n")
                .formatted(Formatting.RED)
                .append(Text.literal("To fix this, edit this server in your Server List and set 'Server Resource Packs: Enabled'.").formatted(Formatting.GRAY))
        );
    }
}
