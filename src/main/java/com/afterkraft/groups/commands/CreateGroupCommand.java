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

public class CreateGroupCommand extends BasicCommand {

    public CreateGroupCommand(Groups plugin) {
        super("CreateGroups", plugin);
        setIdentifiers("create", "groups create", "new");
        setMinArgs(1);
        setMaxArgs(10);
        setPermission("groups.create");
        setDescription("Creates a new group list with added players as parameters");
        setUsage("/groups create <name> [username username2 username3...]");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        if (!(executor instanceof Player)) {
            executor.sendMessage("Sorry, Console can not create groups of it's own.");
            return true;
        }
        Player player = (Player) executor;
        GroupMember member = memberManager.getGroupMember(player);
        if (member.getGroupByName(args[0]) != null) {
            executor.sendMessage("Sorry, you already created a group by the name of " + args[0]);
            return true;
        } else if (Bukkit.getCommandAliases().containsKey(args[0])) {
            executor.sendMessage("You are not allowed to create groups with the names of commands belonging to other plugins!");
            return true;
        } else {
            Group group = new Group(args[0], member);
            executor.sendMessage("Created a new group called " + args[0]);
            if (args.length >= 2) {
                List<String> offlineNames = new ArrayList<String>();
                for (int i = 1; i < args.length; i++) {
                    String name = args[i];
                    OfflinePlayer temp = Bukkit.getOfflinePlayer(name);
                    GroupMember tempMember;
                    if (!temp.isOnline()) {
                        offlineNames.add(args[i]);
                        continue;
                    } else {
                        tempMember = plugin.getStorage().loadGroupMember(temp.getPlayer(), true);
                    }
                    group.addGroupMember(tempMember);
                    executor.sendMessage("Added " + temp.getName() + " to the group: " + group.getName());
                }


                if (!offlineNames.isEmpty()) {
                    try {
                        GroupAddUUIDTask task = new GroupAddUUIDTask(player, member, group, offlineNames);
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
                    } catch (Exception e) {
                        executor.sendMessage("Sorry, we could not add some offline players at this time.");
                    }
                }
            }
            member.addGroup(group);
            plugin.getStorage().saveGroupMember(member);
            return true;
        }
    }

    class GroupAddUUIDTask extends BukkitRunnable {

        List<String> userNames;
        Player player;
        GroupMember member;
        Group group;
        List<GroupMemberInfo> memberArray = new ArrayList<GroupMemberInfo>();

        GroupAddUUIDTask(Player player, GroupMember member, Group group, List<String> userNames) {
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
                Bukkit.getScheduler().runTask(plugin, new SyncGroupAddMemberTask(player, member, group, memberArray));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class SyncGroupAddMemberTask extends BukkitRunnable {

        Player player;
        GroupMember member;
        Group group;
        List<GroupMemberInfo> memberArray = new ArrayList<GroupMemberInfo>();

        SyncGroupAddMemberTask(Player player, GroupMember member, Group group, List<GroupMemberInfo> memberInfoList) {
            this.memberArray = memberInfoList;
            this.player = player;
            this.member = member;
            this.group = group;
        }

        @Override
        public void run() {
            for (GroupMemberInfo info : memberArray) {
                if (!group.hasGroupMemberInfo(info, true)) {
                    group.addGroupMemberInfo(info);
                    player.sendMessage("Added " + info.name + " to the group: " + group.getName());
                } else {
                    player.sendMessage("Sorry, we could not add " + info.name + " to the group: " + group.getName());
                }
            }
            plugin.getStorage().saveGroupMember(member);
        }
    }
}
