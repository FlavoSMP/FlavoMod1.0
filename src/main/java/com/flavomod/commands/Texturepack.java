package com.flavomod;

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
import java.util.Optional;
import java.util.UUID;

public class Texturepack {

    private static final String PACK_URL = "https://your-resourcepack-link.com/pack.zip";
    private static final UUID PACK_UUID = UUID.nameUUIDFromBytes(PACK_URL.getBytes());

    public static void register() {
        // 1. Send prompt on join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            sendChatPrompt(player);
            sendPackPacket(player);
        });

        // 2. Listen for player response (Accept, Decline, Downloaded, Failed)
        ServerPlayNetworking.registerGlobalReceiver(ResourcePackStatusC2SPacket.PACKET_ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            ResourcePackStatusC2SPacket.Status status = payload.status();

            context.server().execute(() -> {
                switch (status) {
                    case ACCEPTED:
                        player.sendMessage(Text.literal(" Downloading FlavoMod textures...").formatted(Formatting.GREEN), false);
                        break;

                    case SUCCESSFUL:
                        // Minecraft automatically applies textures here!
                        player.sendMessage(Text.literal(" Textures applied successfully! Enjoy the Fire Shards! 🔥").formatted(Formatting.GOLD), false);
                        break;

                    case DECLINED:
                        // Kicks the player if they pressed Decline
                        kickPlayerForDeclining(player);
                        break;

                    case FAILED_DOWNLOAD:
                        player.sendMessage(Text.literal("❌ Download failed. Check your internet connection or download link!").formatted(Formatting.RED), false);
                        break;

                    default:
                        break;
                }
            });
        });
    }

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

    private static void sendPackPacket(ServerPlayerEntity player) {
        try {
            Text promptMessage = Text.literal("FlavoMod requires the custom texture pack to display Fire Shards properly!");
            
            ResourcePackSendS2CPacket packet = new ResourcePackSendS2CPacket(
                PACK_UUID,
                URI.create(PACK_URL),
                "", 
                true, // Required = true
                Optional.of(promptMessage)
            );

            player.networkHandler.sendPacket(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void kickPlayerForDeclining(ServerPlayerEntity player) {
        player.networkHandler.disconnect(
            Text.literal("❌ You were disconnected because you declined the required FlavoMod texture pack.\n\n")
                .formatted(Formatting.RED)
                .append(Text.literal("To fix this, edit this server in your Server List and set 'Server Resource Packs: Enabled'.").formatted(Formatting.GRAY))
        );
    }
}
