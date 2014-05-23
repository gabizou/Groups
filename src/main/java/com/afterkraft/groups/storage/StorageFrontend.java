/*
 * Copyright 2014 Gabriel Harris-Rouquette
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterkraft.groups.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.afterkraft.groups.Groups;
import com.afterkraft.groups.groups.GroupMember;

public abstract class StorageFrontend {
    protected final Groups plugin;
    protected final StorageBackend backend;

    /**
     * This map builds up a to-do list for the saving task, which runs every
     * half-hour.
     */
    protected final Map<UUID, GroupMember> toSave;

    /**
     * This map is also a to-do list, but is for the editing of offline
     * players.
     */
    protected final Map<UUID, GroupMember> offlineToSave;

    /**
     * Players in this set are silently dropped by saveGroupMember().
     */
    protected final Set<UUID> ignoredPlayers = new HashSet<UUID>();

    public StorageFrontend(Groups plugin, StorageBackend backend) {
        this.plugin = plugin;
        this.backend = backend;
        toSave = new HashMap<UUID, GroupMember>();
        offlineToSave = new HashMap<UUID, GroupMember>();

        new SavingStarterTask().runTaskTimerAsynchronously(plugin, 20 * 60, 20 * 60);
    }

    /**
     * This constructor skips making a save queue. If you call this
     * constructor, you MUST override saveGroupMember().
     */
    protected StorageFrontend(Groups plugin, StorageBackend backend, boolean ignored) {
        this.plugin = plugin;
        this.backend = backend;
        toSave = null;
        offlineToSave = null;
    }

    /**
     * The name of a StorageFrontend follows the following format:
     * 
     * <pre>
     * [frontend-name]/[backend-name]
     * </pre>
     * 
     * In the default implementation, the frontend-name is "Default". You
     * should change this if you extend the class.
     * 
     * The backend-name is <code>backend.getClass().getSimpleName()</code>.
     * This should remain the same in your implementation.
     * 
     * @return name of storage format
     */
    public String getName() {
        return "Default/" + backend.getClass().getSimpleName();
    }

    /**
     * Load the Champion data.
     * 
     * @param player the requested Player data
     * @return the loaded Champion instance if data exists, else returns null
     */
    public GroupMember loadGroupMember(Player player, boolean shouldCreate) {
        UUID uuid = player.getUniqueId();

        // Check the saving queue for this player
        if (offlineToSave.containsKey(uuid)) {
            PlayerData data = offlineToSave.get(uuid).getData();
            return plugin.getMemberManager().createGroupMember(player, data);
        }
        if (toSave.containsKey(uuid)) {
            GroupMember ret = toSave.get(uuid);
            ret.setPlayer(player);
            return ret;
        }

        PlayerData data = backend.loadPlayer(uuid, shouldCreate);
        if (data == null) {
            if (!shouldCreate) {
                return null;
            } else {
                data = new PlayerData();
            }
        }
        data.playerID = player.getUniqueId();
        data.lastKnownName = player.getName();

        return plugin.getMemberManager().createGroupMember(player, data);
    }

    /**
     * Saves the given {@link com.afterkraft.groups.groups.GroupMember} data
     * at some later point.
     */
    public void saveGroupMember(GroupMember member) {
        if (ignoredPlayers.contains(member.getUniqueId())) {
            return;
        }

        toSave.put(member.getUniqueId(), member);
    }

    public GroupMember loadOfflineGroupMember(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (offlineToSave.containsKey(uuid)) {
            return offlineToSave.get(uuid);
        } else if (toSave.containsKey(uuid)) {
            return toSave.get(uuid);
        } else if (Bukkit.getPlayer(uuid) != null) {
            GroupMember member = plugin.getMemberManager().getGroupMember(uuid);
            if (member != null) {
                return member;
            }
        }
        return plugin.getMemberManager().createOfflineGroupMember(player, backend.loadPlayer(uuid, false));
    }

    public void saveOfflineGroupMember(UUID uuid, GroupMember data) {
        if (toSave.containsKey(uuid)) {
            // XXX This is bad!
        }

        offlineToSave.put(uuid, data);
    }

    public void shutdown() {
        flush();
        backend.shutdown();
    }

    public void flush() {
        for (GroupMember member : toSave.values()) {
            backend.savePlayer(member.getUniqueId(), member.getData());
        }
        toSave.clear();
        for (Map.Entry<UUID, GroupMember> entry : offlineToSave.entrySet()) {
            backend.savePlayer(entry.getKey(), entry.getValue().getData());
        }
        offlineToSave.clear();
    }

    public void ignorePlayer(UUID uuid) {
        ignoredPlayers.add(uuid);
    }

    public void stopIgnoringPlayer(UUID uuid) {
        ignoredPlayers.remove(uuid);
    }

    /**
     * Convert all data from the provided StorageBackend to the one currently
     * being used.
     * 
     * @param from StorageBackend to convert from
     */
    public void doConversion(StorageBackend from) {
        List<UUID> uuids = from.getAllStoredUsers();

        for (UUID uuid : uuids) {
            backend.savePlayer(uuid, from.loadPlayer(uuid, false));
        }
    }

    protected class SavingStarterTask extends BukkitRunnable {
        // Main thread, just like everything else
        public void run() {
            Map<UUID, GroupMember> data = new HashMap<UUID, GroupMember>();

            data.putAll(offlineToSave);
            for (Map.Entry<UUID, GroupMember> entry : toSave.entrySet()) {
                data.put(entry.getKey(), entry.getValue());
            }
            toSave.clear();
            offlineToSave.clear();

            Bukkit.getScheduler().runTaskAsynchronously(plugin, new SavingWorker(data));
        }
    }

    protected class SavingWorker implements Runnable {
        private Map<UUID, GroupMember> data;

        public SavingWorker(Map<UUID, GroupMember> data) {
            this.data = data;
        }

        // ASYNC
        public void run() {
            for (Map.Entry<UUID, GroupMember> entry : data.entrySet()) {
                backend.savePlayer(entry.getKey(), entry.getValue().getData());
            }
        }
    }
}
