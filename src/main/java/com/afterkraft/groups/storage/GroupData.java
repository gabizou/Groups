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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableList;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("groups-group-data")
public class GroupData implements Cloneable, ConfigurationSerializable {

    public String name;
    public UUID ownerID;
    public List<GroupMemberInfo> members = new ArrayList<GroupMemberInfo>();

    public GroupData() {

    }

    @SuppressWarnings("unchecked")
    public static GroupData deserialize(Map<String, Object> map) {
        GroupData data = new GroupData();
        data.name = (String) map.get("group-name");
        data.ownerID = UUID.fromString((String) map.get("ownerID"));
        List<Map<String, Object>> membersList = (List<Map<String, Object>>) map.get("members");
        for (Map<String, Object> memberMap : membersList) {
            GroupMemberInfo memberInfo = new GroupMemberInfo(memberMap);
            data.members.add(memberInfo);
        }
        return data;
    }

    public UUID getOwnerID() {
        return ownerID;
    }

    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("group-name", name);
        map.put("ownerID", ownerID.toString());
        List<Map<String, Object>> membersList = new ArrayList<Map<String, Object>>();
        for (GroupMemberInfo info : members) {
            membersList.add(info.serialize());
        }
        map.put("members", membersList);
        return map;
    }

    @Override
    public int hashCode() {
        return this.name.toLowerCase().hashCode() * this.ownerID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GroupData)) {
            return false;
        } else {
            GroupData info = (GroupData) obj;
            return info.name.equalsIgnoreCase(this.name) && info.ownerID.equals(this.ownerID);
        }
    }

    @Override
    public GroupData clone() {
        GroupData cloned = new GroupData();
        cloned.ownerID = ownerID;
        cloned.name = name;
        cloned.members.addAll(ImmutableList.copyOf(members));
        return cloned;
    }
}
