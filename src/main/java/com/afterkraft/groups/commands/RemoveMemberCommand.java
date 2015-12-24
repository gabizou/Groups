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
package com.afterkraft.groups.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.afterkraft.groups.Groups;
import com.afterkraft.groups.groups.Group;
import com.afterkraft.groups.groups.GroupMember;
import com.afterkraft.groups.storage.GroupMemberInfo;
import com.afterkraft.groups.util.UUIDFetcher;

public class RemoveMemberCommand extends BasicCommand {

    public RemoveMemberCommand(Groups plugin) {
        super("RemoveMember", plugin);
        setMinArgs(2);
        setMaxArgs(10);
        setDescription("Removes players from a specific group list");
        setUsage("/<groupname> remove <playername> [playername2] [playername3]");
        setIdentifiers("removefrom");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        if (!(executor instanceof Player)) {
            executor.sendMessage("Sorry, Console can not create groups of it's own.");
            return true;
        }
        Player player = (Player) executor;
        GroupMember member = memberManager.getGroupMember(player);
        Group group = member.getGroupByName(args[0].toLowerCase());
        if (group == null) {
            executor.sendMessage("Sorry, you don't have any groups by that name.");
            return true;
        }
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        List<String> offlineNames = new ArrayList<String>();
        for (String name : newArgs) {
            OfflinePlayer temp = Bukkit.getOfflinePlayer(name);
            GroupMember tempMember;
            if (!temp.isOnline()) {
                offlineNames.add(name);
                continue;
            } else {
                tempMember = plugin.getStorage().loadOfflineGroupMember(temp.getUniqueId());
            }
            group.removeGroupMember(tempMember);
            executor.sendMessage("Removed " + temp.getName() + " from the group: " + group.getName());
        }
        if (!offlineNames.isEmpty()) {
            try {
                RemoveUUIDTask task = new RemoveUUIDTask(player, member, group, offlineNames);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
            } catch (Exception e) {
                executor.sendMessage("Sorry, we could not add some offline players at this time.");
            }
        }

        plugin.getStorage().saveGroupMember(member);
        return true;
    }

    class RemoveUUIDTask extends BukkitRunnable {

        List<String> userNames;
        Player player;
        GroupMember member;
        Group group;
        List<GroupMemberInfo> memberArray = new ArrayList<GroupMemberInfo>();

        RemoveUUIDTask(Player player, GroupMember member, Group group, List<String> userNames) {
            this.userNames = userNames;
            this.player = player;
            this.member = member;
            this.group = group;
        }

        @Override
        public void run() {
            UUIDFetcher fetcher = new UUIDFetcher(userNames);
            try {
                Map<String, UUID> map = fetcher.call();
                for (Map.Entry<String, UUID> entry : map.entrySet()) {
                    GroupMemberInfo info = new GroupMemberInfo();
                    info.name = entry.getKey();
                    info.playerID = UUID.fromString(entry.getValue().toString());
                    memberArray.add(info);
                }
                if (!userNames.isEmpty() && memberArray.isEmpty()) {
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            for (String name : userNames) {
                                player.sendMessage("Could not validate the username: " + name);
                            }
                        }
                    });
                }
                Bukkit.getScheduler().runTask(plugin, new SyncRemoveMemberThread(player, member, group, memberArray));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class SyncRemoveMemberThread extends BukkitRunnable {

        Player player;
        GroupMember member;
        Group group;
        List<GroupMemberInfo> memberArray = new ArrayList<GroupMemberInfo>();

        SyncRemoveMemberThread(Player player, GroupMember member, Group group, List<GroupMemberInfo> memberInfoList) {
            this.memberArray = memberInfoList;
            this.player = player;
            this.member = member;
            this.group = group;
        }

        @Override
        public void run() {
            for (GroupMemberInfo info : memberArray) {
                if (group.hasGroupMemberInfo(info, false)) {
                    group.removeGroupMemberInfo(info);
                    player.sendMessage("Removed " + info.name + " from the group: " + group.getName());
                }
            }
            plugin.getStorage().saveGroupMember(member);
        }
    }
}
