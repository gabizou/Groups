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

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.afterkraft.groups.storage.PlayerData;

public class GroupMember {

    private transient WeakReference<OfflinePlayer> offlinePlayer;
    private PlayerData data;

    public GroupMember(OfflinePlayer player, PlayerData playerData) {
        this.offlinePlayer = new WeakReference<OfflinePlayer>(player);
        this.data = playerData.clone();
    }

    public Player getPlayer() {
        return this.isOnline() ? this.getOfflinePlayer().getPlayer() : null;
    }

    public void setPlayer(Player player) {
        offlinePlayer = new WeakReference<OfflinePlayer>(player);
        data.lastKnownName = player.getName();
        data.playerID = player.getUniqueId();
    }

    public boolean isOnline() {
        return this.getOfflinePlayer() != null && getOfflinePlayer().isOnline();
    }

    public String getName() {
        return getOfflinePlayer() != null ? getOfflinePlayer().getName() : Bukkit.getOfflinePlayer(data.playerID).getName();
    }

    public OfflinePlayer getOfflinePlayer() {
        return isValid() ? isNull() ? Bukkit.getOfflinePlayer(data.playerID) : offlinePlayer.get() : null;
    }

    public boolean isValid() {
        return offlinePlayer.get() != null || Bukkit.getOfflinePlayer(data.playerID).hasPlayedBefore();
    }

    private boolean isNull() {
        return offlinePlayer.get() == null;
    }

    public boolean addGroup(Group group) {
        if (group != null && !data.groupMap.containsKey(group.getName())) {
            data.groupMap.put(group.getName(), group);
            return true;
        }
        return false;
    }

    public Group getGroupByName(String groupName) {
        return groupName != null ? data.groupMap.get(groupName) : null;
    }

    public boolean removeGroup(String groupName) {
        return groupName != null && data.groupMap.remove(groupName) != null;
    }

    public Set<Group> getGroups() {
        return ImmutableSet.copyOf(data.groupMap.values());
    }

    public Set<String> getGroupNames() {
        return ImmutableSet.copyOf(data.groupMap.keySet());
    }

    public PlayerData getData() {
        return data;
    }

    public PlayerData getDataClone() {
        return data.clone();
    }

    @Override
    public int hashCode() {
        return getUniqueId().hashCode();
    }

    public UUID getUniqueId() {
        return data.playerID;
    }

    @Override
    public String toString() {
        return getOfflinePlayer() != null ? getOfflinePlayer().getName() : getUniqueId().toString();
    }
}
