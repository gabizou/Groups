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
package com.afterkraft.groups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.afterkraft.groups.groups.Group;
import com.afterkraft.groups.groups.GroupMember;

public class PlayerListener implements Listener {

    private Groups plugin;

    public PlayerListener(Groups plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        plugin.memberManager.getGroupMember(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String input = event.getMessage().substring(1);
        String[] args = input.split(" ");
        GroupMember member = plugin.memberManager.getGroupMember(event.getPlayer());
        Group group = member.getGroupByName(args[0]);
        if (group != null && !Bukkit.getCommandAliases().containsKey(args[0])) {
            event.setCancelled(true);
            plugin.getLogger().log(Level.INFO, "label: " + args[0]);
            String argString = "args: ";
            for (int i = 0; i < args.length; i++) {
                if (i == 0) {
                    argString += "[ " + args[i];
                } else {
                    argString += args[i];
                }
                if (i == args.length - 1) {
                    argString += " ]";
                } else {
                    argString += " , ";
                }
            }
            plugin.getLogger().log(Level.INFO, "args: " + argString);
            if (args.length < 2) {
                plugin.commandHandler.executeCommand(event.getPlayer(), "groups", "help", args);
            } else {
                String[] newArgs = new String[args.length];
                String newIdentifier = "?";

                if (args[1].equalsIgnoreCase("add")) {
                    if (args.length < 2) {
                        plugin.commandHandler.executeCommand(event.getPlayer(), "groups", "addto", new String[] { "?" });
                    }
                    newIdentifier = "addto";
                    ArrayList<String> tempargs = new ArrayList<String>();
                    tempargs.add("addto");
                    tempargs.add(group.getName());
                    tempargs.addAll(Arrays.asList(Arrays.copyOfRange(args, 2, args.length)));
                    newArgs = new String[tempargs.size()];
                    newArgs = tempargs.toArray(newArgs);
                } else if (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("delete")) {
                    if (args.length < 2) {
                        plugin.commandHandler.executeCommand(event.getPlayer(), "groups", "removefrom", new String[] { "?" });
                    }
                    newIdentifier = "removefrom";
                    ArrayList<String> tempargs = new ArrayList<String>();
                    tempargs.add("removefrom");
                    tempargs.add(group.getName());
                    tempargs.addAll(Arrays.asList(Arrays.copyOfRange(args, 2, args.length)));
                    newArgs = new String[tempargs.size()];
                    newArgs = tempargs.toArray(newArgs);
                } else if (args[1].equalsIgnoreCase("list")) {
                    newIdentifier = "listgroup";
                    ArrayList<String> tempargs = new ArrayList<String>();
                    tempargs.add("listgroup");
                    tempargs.add(group.getName());
                    newArgs = new String[tempargs.size()];
                    newArgs = tempargs.toArray(newArgs);
                }
                plugin.commandHandler.executeCommand(event.getPlayer(), "groups ", newIdentifier, newArgs);
            }
        }
    }
}
