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

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.afterkraft.groups.Groups;
import com.afterkraft.groups.groups.Group;
import com.afterkraft.groups.groups.GroupMember;
import com.afterkraft.groups.storage.PlayerData;
import com.afterkraft.groups.util.UUIDFetcher;

public class AddMemberCommand extends BasicCommand {

    public AddMemberCommand(Groups plugin) {
        super("AddMember", plugin);
        setMinArgs(2);
        setMaxArgs(10);
        setDescription("Adds players to a specific group list");
        setUsage("/<groupname> add <playername> [playername2] [playername3]");
        setIdentifiers("addto");
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
        for (int i = 1; i < args.length; i++) {
            OfflinePlayer temp = Bukkit.getOfflinePlayer(args[i]);
            GroupMember tempMember;
            if (!temp.isOnline()) {
                try {
                    UUID uuid = UUIDFetcher.getUUIDOf(args[i]);
                    PlayerData data = new PlayerData();
                    data.playerID = uuid;
                    data.lastKnownName = args[i];
                    tempMember = new GroupMember(temp, data);
                } catch (Exception e) {
                    executor.sendMessage("Sorry, we could not find a player by the name:" + args[i]);
                    continue;
                }
            } else {
                tempMember = plugin.getStorage().loadGroupMember(temp.getPlayer(), true);
            }
            group.addGroupMember(tempMember);
            executor.sendMessage("Added " + temp.getName() + " to the group: " + group.getName());
        }
        plugin.getStorage().saveGroupMember(member);
        return true;
    }
}
