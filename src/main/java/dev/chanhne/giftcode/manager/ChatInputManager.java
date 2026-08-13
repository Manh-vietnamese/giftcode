package dev.chanhne.giftcode.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatInputManager {

    private final Map<UUID, Input> inputs = new HashMap<>();
    public enum Type {VAULT, SHARD, COMMAND}

    private static class Input {
        private final Type type;

        private Input(Type type) {this.type = type;}
        public Type getType() {return type;}
    }

    public void start(UUID uuid, Type type) {
        inputs.put(uuid, new Input(type));
    }

    public boolean has(UUID uuid) {
        return inputs.containsKey(uuid);
    }

    public Type getType(UUID uuid) {
        Input input = inputs.get(uuid);
        return input == null ? null : input.getType();
    }

    public void remove(UUID uuid) {
        inputs.remove(uuid);
    }

}