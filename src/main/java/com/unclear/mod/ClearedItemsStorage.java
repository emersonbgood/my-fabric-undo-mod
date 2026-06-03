package com.unclear.mod;

import net.minecraft.item.ItemStack;

import java.util.*;

/**
 * Server-side in-memory store of items removed by /clear per player UUID.
 * Each /unclear or keybind press pops the most recent snapshot.
 */
public class ClearedItemsStorage {

    // UUID -> ordered list of snapshots (oldest first, newest last)
    private static final Map<UUID, Deque<List<ItemStack>>> history = new HashMap<>();

    /**
     * Push a new snapshot of cleared items for a player.
     * Call this right before /clear removes items, passing only the stacks
     * that WILL be removed (computed by the mixin).
     */
    public static void push(UUID playerId, List<ItemStack> removed) {
        if (removed.isEmpty()) return;
        history.computeIfAbsent(playerId, k -> new ArrayDeque<>()).addLast(removed);
        UnclearMod.LOGGER.info("[Unclear] Stored {} item type(s) cleared from {}",
                removed.size(), playerId);
    }

    /**
     * Pop the most recent snapshot for a player (used by /unclear or Y key).
     * Returns an empty list if nothing is stored.
     */
    public static List<ItemStack> pop(UUID playerId) {
        Deque<List<ItemStack>> queue = history.get(playerId);
        if (queue == null || queue.isEmpty()) return Collections.emptyList();
        List<ItemStack> snapshot = queue.removeLast();
        if (queue.isEmpty()) history.remove(playerId);
        return snapshot;
    }

    /** True if the player has at least one saved snapshot. */
    public static boolean has(UUID playerId) {
        Deque<List<ItemStack>> queue = history.get(playerId);
        return queue != null && !queue.isEmpty();
    }

    /** How many snapshots exist for a player. */
    public static int count(UUID playerId) {
        Deque<List<ItemStack>> queue = history.get(playerId);
        return queue == null ? 0 : queue.size();
    }
}
