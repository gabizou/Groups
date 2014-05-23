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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("group-member")
public class GroupMemberInfo implements Cloneable, ConfigurationSerializable {
    public String name;
    public UUID playerID;

    public GroupMemberInfo() {

    }

    public GroupMemberInfo(Map<String, Object> map) {
        name = (String) map.get("playerName");
        playerID = UUID.fromString((String) map.get("playerID"));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("playerName", name);
        map.put("playerID", playerID.toString());
        return map;
    }

    @Override
    public int hashCode() {
        return this.name.toLowerCase().hashCode() * this.playerID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GroupMemberInfo)) {
            return false;
        } else {
            GroupMemberInfo info = (GroupMemberInfo) obj;
            return info.name.equalsIgnoreCase(this.name) && info.playerID.equals(this.playerID);
        }
    }

    @Override
    public GroupMemberInfo clone() {
        GroupMemberInfo info = new GroupMemberInfo();
        info.name = name;
        info.playerID = playerID;
        return info;
    }
}
