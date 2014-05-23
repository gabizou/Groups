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
package com.afterkraft.groups.groups;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.afterkraft.groups.Groups;
import com.afterkraft.groups.Manager;
import com.afterkraft.groups.storage.PlayerData;

public class MemberManager implements Manager {

    private final Groups plugin;
    private WeakHashMap<UUID, GroupMember> cachedMembers = new WeakHashMap<UUID, GroupMember>();
    private int id;

    public MemberManager(Groups plugin) {
        this.plugin = plugin;
        id = Bukkit.getScheduler().runTaskTimer(plugin, new CacheCleaningTask(), 1000, 6000).getTaskId();
    }

    public void shutdown() {
        if (id != 0) {
            Bukkit.getScheduler().cancelTask(id);
        }
        for (GroupMember member : cachedMembers.values()) {
            plugin.getStorage().saveGroupMember(member);
        }
        cachedMembers.clear();
    }

    public GroupMember createGroupMember(Player player, PlayerData data) {
        return createOfflineGroupMember(player, data);
    }

    public GroupMember createOfflineGroupMember(OfflinePlayer player, PlayerData data) {
        GroupMember member = new GroupMember(player, data);
        cachedMembers.put(player.getUniqueId(), member);
        return member;
    }

    public GroupMember getGroupMember(UUID uniqueID) {
        if (uniqueID == null) {
            return null;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(uniqueID);
        if (player.isOnline()) {
            return getGroupMember(player.getPlayer());
        } else if (player.hasPlayedBefore()) {
            if (cachedMembers.containsKey(uniqueID)) {
                return cachedMembers.get(uniqueID);
            } else {
                GroupMember member = plugin.getStorage().loadOfflineGroupMember(uniqueID);
                return member != null ? member : null;
            }
        } else {
            return null;
        }
    }

    public GroupMember getGroupMember(Player player) {
        if (player == null) {
            return null;
        }
        if (cachedMembers.containsKey(player.getUniqueId())) {
            return cachedMembers.get(player.getUniqueId());
        } else {
            // Load from Storage
            GroupMember member = plugin.getStorage().loadGroupMember(player, true);
            if (member == null) {
                member = new GroupMember(player, new PlayerData());
            }
            cachedMembers.put(player.getUniqueId(), member);
            return member;
        }
    }

    private class CacheCleaningTask extends BukkitRunnable {
        @Override
        public void run() {
            Iterator<Map.Entry<UUID, GroupMember>> iterator = cachedMembers.entrySet().iterator();
            while (iterator.hasNext()) {
                GroupMember member = iterator.next().getValue();
                if (!member.isValid()) {
                    iterator.remove();
                }
            }
        }
    }
}
