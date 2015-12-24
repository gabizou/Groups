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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.afterkraft.groups.Groups;
import com.afterkraft.groups.groups.GroupMember;

public class RemoveGroupCommand extends BasicCommand {

    public RemoveGroupCommand(Groups plugin) {
        super("RemoveGroup", plugin);
        setIdentifiers("remove", "groups remove", "delete");
        setMinArgs(2);
        setMaxArgs(10);
        setPermission("groups.delete");
        setUsage("/groups remove <name> [username username2 username3...]");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        if (!(executor instanceof Player)) {
            executor.sendMessage("Sorry, Console can not create groups of it's own.");
            return true;
        }
        Player player = (Player) executor;
        GroupMember member = memberManager.getGroupMember(player);
        if (member.getGroupByName(args[0]) == null) {
            executor.sendMessage("Sorry, there is no group by the name of " + args[0]);
            return true;
        } else {
            member.removeGroup(args[0]);
            executor.sendMessage("Removed the group " + args[0]);
            return true;
        }
    }
}
