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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.afterkraft.groups.Groups;
import com.afterkraft.groups.Manager;

public class CommandHandler implements Manager {

    private Groups plugin;
    private Map<String, BasicCommand> commandMap = new LinkedHashMap<String, BasicCommand>();

    public CommandHandler(Groups plugin) {
        this.plugin = plugin;
    }

    public boolean addCommand(BasicCommand command) {
        if (command == null || commandMap.containsKey(command.getName())) {
            return false;
        }
        commandMap.put(command.getName(), command);
        return true;
    }

    public boolean executeCommand(CommandSender sender, String label, String ident, String[] args) {
        String[] arguments;
        if (args.length < 1) {
            arguments = new String[] { label };
        } else {
            arguments = args;
        }

        for (int argsIncluded = arguments.length; argsIncluded >= 0; argsIncluded--) {
            String identifier = "";
            for (int i = 0; i < argsIncluded; i++) {
                identifier += " " + arguments[i];
            }

            if (ident != null && !ident.isEmpty()) {
                identifier = ident;
            } else {
                identifier = identifier.trim();
            }

            for (BasicCommand cmd : commandMap.values()) {

                if (cmd.isIdentifier(identifier)) {
                    String[] remove = new String[] {};
                    for (String identifiers : cmd.getIdentifiers()) {
                        if (identifier.equalsIgnoreCase(identifiers))
                            remove = identifier.split(" ");
                    }
                    if (remove.length == 0) {
                        remove = new String[] { "" };
                    }
                    String[] realArgs = Arrays.copyOfRange(arguments, remove.length, arguments.length);

                    if (realArgs.length < cmd.getMinArgs() || realArgs.length > cmd.getMaxArgs()) {
                        displayCommandHelp(cmd, sender);
                        return true;
                    } else if (realArgs.length > 0 && realArgs[0] != null && realArgs[0].equals("?")) {
                        displayCommandHelp(cmd, sender);
                        return true;
                    }

                    if (!hasPermission(sender, cmd.getPermission())) {
                        sender.sendMessage("Insufficient Permission");
                        return true;
                    }

                    plugin.getLogger().log(Level.FINE, "Command " + cmd.getName() + " Identifier: " +
                            Arrays.toString(cmd.getIdentifiers()) + " using Identifier: " + identifier +
                            " with Args: " + Arrays.toString(realArgs));
                    cmd.execute(sender, identifier, realArgs);
                    return true;
                }
            }
        }
        plugin.getLogger().log(Level.FINE, "No Command Identifier with Args: " + Arrays.toString(arguments));
        return true;

    }

    private void displayCommandHelp(BasicCommand cmd, CommandSender sender) {
        sender.sendMessage(ChatColor.BLUE + "Command: " + ChatColor.GREEN + cmd.getName());
        sender.sendMessage(ChatColor.BLUE + "Description: " + ChatColor.GREEN + cmd.getDescription());
        sender.sendMessage(ChatColor.BLUE + "Usage: " + ChatColor.GREEN + cmd.getUsage());
    }

    public static boolean hasPermission(CommandSender sender, String permission) {
        if (!(sender instanceof Player) || permission == null || permission.isEmpty()) {
            return true;
        }
        return sender.hasPermission(permission);
    }

    @Override
    public void shutdown() {
        commandMap.clear();
    }
}
