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
package com.afterkraft.groups.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

import com.afterkraft.groups.Groups;

public class YMLStorage implements StorageBackend {
    private File membersFolder;

    private Groups plugin;

    public YMLStorage(Groups groups) {
        this.plugin = groups;
    }

    @Override
    public void initialize() throws Throwable {
        this.membersFolder = new File(plugin.getDataFolder() + File.separator + "players");
        membersFolder.mkdir();
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean removePlayer(UUID uuid) {
        File file = getFile(uuid);
        return file.delete();
    }

    @Override
    public boolean savePlayer(UUID uuid, PlayerData data) {
        File file = getFile(uuid);
        YamlConfiguration config = new YamlConfiguration();
        config.set("player", data);
        List<GroupData> groupList = new ArrayList<GroupData>();
        for (GroupData groupData : data.groupMap.values()) {
            groupList.add(groupData);
        }
        config.set("groups", groupList);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PlayerData loadPlayer(UUID uuid, boolean shouldCreate) {
        File file = getFile(uuid);
        if (!file.exists()) {
            return shouldCreate ? new PlayerData() : null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        PlayerData data;
        data = (PlayerData) config.get("player");
        List<GroupData> groups = (List<GroupData>) config.get("groups");
        for (GroupData groupData : groups) {
            data.groupMap.put(groupData.name, groupData);
        }
        return data;
    }

    @Override
    public List<UUID> getAllStoredUsers() {
        File[] files = membersFolder.listFiles();
        List<UUID> list = new ArrayList<UUID>();

        for (File file : files) {
            try {
                list.add(UUID.fromString(removeExtension(file.getName())));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Could not parse uuid from filename '" + file.getName() + "'");
            }
        }

        return list;
    }

    /**
     * Removes the extension from a filename.
     * <p>
     * This method returns the textual part of the filename before the last
     * dot. There must be no directory separator after the dot.
     * 
     * <pre>
     * foo.txt    --> foo
     * a\b\c.jpg  --> a\b\c
     * a\b\c      --> a\b\c
     * a.b\c      --> a.b\c
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code
     * is running on.
     * 
     * @param filename the filename to query, null returns null
     * @return the filename minus the extension
     */
    private static String removeExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index;
        // indexOfExtension(String filename)
        int extensionPos = filename.lastIndexOf('.');
        int lastSeparator;
        // indexOfLastSeparator(String filename)
        int lastUnixPos = filename.lastIndexOf('/');
        int lastWindowsPos = filename.lastIndexOf('\\');
        lastSeparator = Math.max(lastUnixPos, lastWindowsPos);
        // end indexOfLastSeparator
        index = lastSeparator > extensionPos ? -1 : extensionPos;
        // end indexOfExtension

        if (index == -1) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }

    private File getFile(UUID id) {
        return new File(membersFolder, id.toString() + ".yml");
    }

    /*
     * The method below is taken from commons.io's FilenameUtils under the
     * terms of the Apache 2.0 License. The license notice in the original
     * source appears below.
     */
    //-----------------------------------------------------------------------
    /*
     * This method is licensed to the Apache Software Foundation (ASF) under
     * one or more contributor license agreements. See the NOTICE file
     * distributed with this work for additional information regarding
     * copyright ownership. The ASF licenses this file to You under the Apache
     * License, Version 2.0 (the "License"); you may not use this file except
     * in compliance with the License. You may obtain a copy of the License at
     * 
     * http://www.apache.org/licenses/LICENSE-2.0
     * 
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
     * implied. See the License for the specific language governing
     * permissions and limitations under the License.
     */

    private boolean createBackupFile(UUID uuid) {
        File newFile = new File(membersFolder, "backup-" + uuid.toString() + ".yml");
        if (newFile.exists()) {
            long last = newFile.lastModified();
            long duration = System.currentTimeMillis() - last;

            // 1 week
            if (duration > 7 * 24 * 60 * 60 * 1000) {
                plugin.getLogger().warning("Overwriting week-old previous backup file");
                newFile.delete();
            } else {
                return false;
            }
        }

        getFile(uuid).renameTo(newFile);
        return true;
    }
    //-----------------------------------------------------------------------
}
