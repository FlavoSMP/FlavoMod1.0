package com.flavomod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Currency extends SavedData {

    private static final String DATA_NAME = "flavomod_currency";

    private final Map<UUID, Long> money = new HashMap<>();
    private final Map<UUID, Long> fireShards = new HashMap<>();

    // =========================================================================
    // CREATE / LOAD
    // =========================================================================

    public static Currency create() {
        return new Currency();
    }

    public static Currency load(
            CompoundTag tag,
            net.minecraft.core.HolderLookup.Provider registries
    ) {
        Currency currency = new Currency();

        CompoundTag moneyTag = tag.getCompound("Money");
        CompoundTag shardsTag = tag.getCompound("FireShards");

        for (String uuidString : moneyTag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                currency.money.put(
                        uuid,
                        moneyTag.getLong(uuidString)
                );
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid UUID entries.
            }
        }

        for (String uuidString : shardsTag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                currency.fireShards.put(
                        uuid,
                        shardsTag.getLong(uuidString)
                );
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid UUID entries.
            }
        }

        return currency;
    }

    // =========================================================================
    // SAVE
    // =========================================================================

    @Override
    public CompoundTag save(
            CompoundTag tag,
            net.minecraft.core.HolderLookup.Provider registries
    ) {
        CompoundTag moneyTag = new CompoundTag();
        CompoundTag shardsTag = new CompoundTag();

        for (Map.Entry<UUID, Long> entry : money.entrySet()) {
            moneyTag.putLong(
                    entry.getKey().toString(),
                    entry.getValue()
            );
        }

        for (Map.Entry<UUID, Long> entry : fireShards.entrySet()) {
            shardsTag.putLong(
                    entry.getKey().toString(),
                    entry.getValue()
            );
        }

        tag.put("Money", moneyTag);
        tag.put("FireShards", shardsTag);

        return tag;
    }

    // =========================================================================
    // GET CURRENCY DATA
    // =========================================================================

    public long getMoney(UUID playerUuid) {
        return money.getOrDefault(playerUuid, 0L);
    }

    public long getFireShards(UUID playerUuid) {
        return fireShards.getOrDefault(playerUuid, 0L);
    }

    // =========================================================================
    // SET CURRENCY
    // =========================================================================

    public void setMoney(UUID playerUuid, long amount) {
        money.put(playerUuid, Math.max(0, amount));
        setDirty();
    }

    public void setFireShards(UUID playerUuid, long amount) {
        fireShards.put(playerUuid, Math.max(0, amount));
        setDirty();
    }

    // =========================================================================
    // ADD CURRENCY
    // =========================================================================

    public void addMoney(UUID playerUuid, long amount) {
        long current = getMoney(playerUuid);

        setMoney(
                playerUuid,
                Math.max(0, current + amount)
        );
    }

    public void addFireShards(UUID playerUuid, long amount) {
        long current = getFireShards(playerUuid);

        setFireShards(
                playerUuid,
                Math.max(0, current + amount)
        );
    }

    // =========================================================================
    // REMOVE CURRENCY
    // =========================================================================

    public boolean removeMoney(UUID playerUuid, long amount) {
        if (amount < 0) {
            return false;
        }

        long current = getMoney(playerUuid);

        if (current < amount) {
            return false;
        }

        setMoney(
                playerUuid,
                current - amount
        );

        return true;
    }

    public boolean removeFireShards(UUID playerUuid, long amount) {
        if (amount < 0) {
            return false;
        }

        long current = getFireShards(playerUuid);

        if (current < amount) {
            return false;
        }

        setFireShards(
                playerUuid,
                current - amount
        );

        return true;
    }

    // =========================================================================
    // SERVER ACCESS
    // =========================================================================

    public static Currency get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<>(
                                Currency::create,
                                Currency::load,
                                null
                        ),
                        DATA_NAME
                );
    }
}
