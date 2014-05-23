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
package com.afterkraft.groups.groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableList;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("playerGroupList")
public class Group implements ConfigurationSerializable {

    GroupMember owner;
    private UUID ownerID;
    private List<UUID> members = new ArrayList<UUID>();
    private String name;

    Group(Map<String, Object> map) {
        this(UUID.fromString((String) map.get("ownerID")), (String) map.get("groupName"), (List<String>) map.get("members"));
    }

    public Group(UUID ownerID, String name, List<String> ids) {
        this.ownerID = ownerID;
        this.name = name;
        for (String id : ids) {
            this.members.add(UUID.fromString(id));
        }
    }

    public Group(String name, GroupMember owner) {
        this.name = name;
        this.owner = owner;
        this.ownerID = owner.getUniqueId();
    }

    public void addGroupMember(GroupMember member) {
        if (member == null || member.equals(owner)) {
            return;
        }
        this.members.add(member.getUniqueId());
    }

    public void removeGroupMember(GroupMember member) {
        if (member == null || member.equals(owner)) {
            return;
        }
        this.members.remove(member.getUniqueId());
    }

    public boolean hasGroupMember(GroupMember member) {
        return this.members.contains(member.getUniqueId());
    }

    public String getName() {
        return this.name;
    }

    public GroupMember getOwner() {
        return this.owner;
    }

    public List<UUID> getMembers() {

        return ImmutableList.copyOf(this.members);
    }


    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("ownerID", this.ownerID.toString());
        map.put("groupName", this.name);
        List<String> idList = new ArrayList<String>();
        for (UUID uuid : members) {
            if (uuid != null) {
                idList.add(uuid.toString());
            }
        }
        map.put("members", idList.isEmpty() ? new ArrayList<String>() : idList);
        return map;
    }
}
