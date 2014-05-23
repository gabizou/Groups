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

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.afterkraft.groups.Groups;
import com.afterkraft.groups.groups.Group;
import com.afterkraft.groups.groups.GroupMember;

public class RemoveMemberCommand extends BasicCommand {

    public RemoveMemberCommand(Groups plugin) {
        super("RemoveMember", plugin);
        setMinArgs(3);
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
        String[] newArgs = Arrays.copyOfRange(args, 2, args.length - 1);
        for (String name : newArgs) {
            OfflinePlayer temp = Bukkit.getOfflinePlayer(name);
            if (!temp.hasPlayedBefore()) {
                executor.sendMessage("Sorry, " + name + " has not played before!");
                return true;
            } else {
                GroupMember tempMember = plugin.getStorage().loadOfflineGroupMember(temp.getUniqueId());
                group.removeGroupMember(tempMember);
                executor.sendMessage("Removed " + temp.getName() + " from the group: " + group.getName());
            }
        }
        plugin.getStorage().saveGroupMember(member);
        return true;
    }
}
