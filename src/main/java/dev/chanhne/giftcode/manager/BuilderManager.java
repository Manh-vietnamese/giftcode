package dev.chanhne.giftcode.manager;

import dev.chanhne.giftcode.builder.GiftCodeBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuilderManager {

    private final Map<UUID, GiftCodeBuilder> builders = new HashMap<>();

    public void create(UUID uuid, GiftCodeBuilder builder) {
        builders.put(uuid, builder);
    }

    public GiftCodeBuilder get(UUID uuid) {
        return builders.get(uuid);
    }

    public boolean has(UUID uuid) {
        return builders.containsKey(uuid);
    }

    public void remove(UUID uuid) {
        builders.remove(uuid);
    }
}