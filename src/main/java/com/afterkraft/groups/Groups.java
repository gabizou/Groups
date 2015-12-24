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

import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.afterkraft.groups.commands.AddMemberCommand;
import com.afterkraft.groups.commands.CommandHandler;
import com.afterkraft.groups.commands.CreateGroupCommand;
import com.afterkraft.groups.commands.ListGroupCommand;
import com.afterkraft.groups.commands.ListMemberCommand;
import com.afterkraft.groups.commands.RemoveGroupCommand;
import com.afterkraft.groups.commands.RemoveMemberCommand;
import com.afterkraft.groups.groups.MemberManager;
import com.afterkraft.groups.storage.GroupData;
import com.afterkraft.groups.storage.GroupMemberInfo;
import com.afterkraft.groups.storage.PlayerData;
import com.afterkraft.groups.storage.StorageFrontend;
import com.afterkraft.groups.storage.StorageManager;
import com.afterkraft.groups.storage.YMLStorage;
import com.afterkraft.groups.util.ExternalProviderRegistration;

public final class Groups extends JavaPlugin {

    static {
        ConfigurationSerialization.registerClass(GroupData.class, "groups-group-data");
        ConfigurationSerialization.registerClass(PlayerData.class, "groups-player-data");
        ConfigurationSerialization.registerClass(GroupMemberInfo.class, "group-member-info");
    }
    private static boolean cancel = false;
    protected MemberManager memberManager;
    protected CommandHandler commandHandler;
    protected StorageFrontend storage;
    protected StorageManager storageManager;

    public StorageFrontend getStorage() {
        return storageManager.getStorage();
    }


    public MemberManager getMemberManager() {
        return this.memberManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Sorry, Groups isn't written to handle console commands yet!");
            return true;
        } else {
            getLogger().log(Level.INFO, "label: " + label);
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
            getLogger().log(Level.INFO, "args: " + argString);
            return this.commandHandler.executeCommand(sender, label, null, args);
        }
    }

    @Override
    public void onLoad() {
        ConfigurationSerialization.registerClass(GroupData.class, "groups-group-data");
        ConfigurationSerialization.registerClass(PlayerData.class, "groups-player-data");
        ExternalProviderRegistration.pluginLoaded(this);
        ExternalProviderRegistration.registerStorageBackend(new YMLStorage(this), "yml", "yaml");
    }

    @Override
    public void onDisable() {
        try {
            this.commandHandler.shutdown();
            this.memberManager.shutdown();
            this.storageManager.shutdown();
            HandlerList.unregisterAll(this);
        } catch (Exception e) {
            getLogger().warning("------------------------------------------------");
            getLogger().warning("|--- Something did not shut down correctly! ---|");
            getLogger().warning("|-- Please make sure to report the following --|");
            getLogger().warning("|---------- error to the Groups devs ----------|");
            e.printStackTrace();
            getLogger().warning("|----------------------------------------------|");
            getLogger().warning("|---------------- End of Error ----------------|");
            getLogger().warning("------------------------------------------------");
        }
    }

    @Override
    public void onEnable() {
        this.storageManager = new StorageManager(this);
        this.storageManager.initialize();
        if (cancel) return;
        this.memberManager = new MemberManager(this);
        this.commandHandler = new CommandHandler(this);
        registerCommands();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    private void registerCommands() {
        this.commandHandler.addCommand(new CreateGroupCommand(this));
        this.commandHandler.addCommand(new RemoveGroupCommand(this));
        this.commandHandler.addCommand(new ListGroupCommand(this));
        this.commandHandler.addCommand(new AddMemberCommand(this));
        this.commandHandler.addCommand(new RemoveMemberCommand(this));
        this.commandHandler.addCommand(new ListMemberCommand(this));
    }

    public void cancelEnable() {
        cancel = true;
        getServer().getPluginManager().disablePlugin(this);
    }
}
