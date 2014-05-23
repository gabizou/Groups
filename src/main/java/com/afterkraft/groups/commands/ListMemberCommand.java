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

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.afterkraft.groups.Groups;
import com.afterkraft.groups.groups.Group;
import com.afterkraft.groups.groups.GroupMember;

public class ListMemberCommand extends BasicCommand {

    public ListMemberCommand(Groups plugin) {
        super("ListMembers", plugin);
        setMinArgs(1);
        setMaxArgs(1);
        setDescription("List players belonging to a specific group");
        setUsage("/<groupname> list");
        setIdentifiers("listgroup");
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
        String message = ChatColor.YELLOW + group.getName() + ChatColor.GREEN + ": ";
        List<UUID> members = group.getMembers();
        for (UUID temp : members) {
            message += Bukkit.getOfflinePlayer(temp).getName() + " ";
        }
        executor.sendMessage(message);
        return true;
    }
}
