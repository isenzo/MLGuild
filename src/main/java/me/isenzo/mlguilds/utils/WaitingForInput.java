package me.isenzo.mlguilds.utils;

import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WaitingForInput {
    private static final Map<UUID, InputType> waitingForType = new HashMap<>();
    private static final Map<UUID, Material> waitingForMaterial = new HashMap<>();

    public static void add(UUID player, InputType type) {
        waitingForType.put(player, type);
    }

    public static void add(UUID player, Material material) {
        waitingForMaterial.put(player, material);
        waitingForType.put(player, InputType.ITEM);
    }

    public static boolean isWaitingForInput(UUID player, InputType type) {
        return waitingForType.getOrDefault(player, null) == type;
    }

    public static Material getMaterialForPlayer(UUID player) {
        return waitingForMaterial.get(player);
    }

    public static void remove(UUID player, InputType type) {
        if (isWaitingForInput(player, type)) {
            waitingForType.remove(player);
            if (type == InputType.ITEM) {
                waitingForMaterial.remove(player);
            }
        }
    }
}
