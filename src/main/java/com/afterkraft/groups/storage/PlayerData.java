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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("groups-player-data")
public class PlayerData implements Cloneable, ConfigurationSerializable {

    public final Map<String, GroupData> groupMap = new HashMap<String, GroupData>();
    public UUID playerID;
    public String lastKnownName;

    public PlayerData() {

    }

    public static PlayerData deserialize(Map<String, Object> map) {
        PlayerData data = new PlayerData();
        data.lastKnownName = (String) map.get("last-known-name");
        data.playerID = UUID.fromString((String) map.get("playerID"));
        return data;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("last-known-name", lastKnownName);
        map.put("playerID", playerID.toString());
        return map;
    }

    @Override
    public int hashCode() {
        return this.playerID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PlayerData)) {
            return false;
        } else {
            PlayerData info = (PlayerData) obj;
            return info.playerID.equals(this.playerID);
        }
    }

    public PlayerData clone() {
        PlayerData cloned = new PlayerData();
        cloned.playerID = playerID;
        cloned.lastKnownName = lastKnownName;
        cloned.groupMap.putAll(groupMap);
        return cloned;
    }
}
