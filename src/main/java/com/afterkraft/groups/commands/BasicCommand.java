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

import org.bukkit.command.CommandSender;

import com.afterkraft.groups.Groups;
import com.afterkraft.groups.groups.MemberManager;

public abstract class BasicCommand {

    protected final Groups plugin;
    protected final MemberManager memberManager;
    private final String name;
    private String[] identifiers;
    private int minArgs;
    private int maxArgs;
    private String permission;
    private String description;
    private String usage;

    public BasicCommand(String name, Groups plugin) {
        this.name = name;
        this.plugin = plugin;
        this.memberManager = plugin.getMemberManager();
    }

    public String[] getIdentifiers() {
        return Arrays.copyOf(this.identifiers, this.identifiers.length);
    }

    protected void setIdentifiers(String... identifiers) {
        this.identifiers = identifiers;
    }

    public int getMinArgs() {
        return this.minArgs;
    }

    protected void setMinArgs(int min) {
        this.minArgs = min;
    }

    public int getMaxArgs() {
        return this.maxArgs;
    }

    protected void setMaxArgs(int max) {
        this.maxArgs = max;
    }

    public boolean isIdentifier(String query) {
        for (final String identifier : identifiers) {
            if (query.equalsIgnoreCase(identifier)) {
                return true;
            }
        }
        return false;
    }

    public String getPermission() {
        return this.permission;
    }

    protected void setPermission(String perm) {
        this.permission = perm;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public String getUsage() {
        return usage;
    }

    protected void setUsage(String usage) {
        this.usage = usage;
    }

    public abstract boolean execute(CommandSender executor, String identifier, String[] args);


}
