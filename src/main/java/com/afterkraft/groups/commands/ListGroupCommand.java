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

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.afterkraft.groups.Groups;
import com.afterkraft.groups.groups.GroupMember;

public class ListGroupCommand extends BasicCommand {

    public ListGroupCommand(Groups plugin) {
        super("ListGroup", plugin);
        setIdentifiers("list", "groups list", "show");
        setMinArgs(0);
        setMaxArgs(0);
        setPermission("groups.list");
        setUsage("/groups list");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        Player player = (Player) executor;
        GroupMember member = memberManager.getGroupMember(player);

        String message = ChatColor.YELLOW + player.getName() + ChatColor.GREEN + ": ";
        for (String group : member.getGroupNames()) {
            message += group + " ";
        }
        executor.sendMessage(message);
        return true;
    }
}
