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

import java.util.List;

import com.google.common.collect.ImmutableList;

import com.afterkraft.groups.storage.GroupData;
import com.afterkraft.groups.storage.GroupMemberInfo;
import com.afterkraft.groups.storage.PlayerData;

public class Group {

    private GroupMember owner;
    private GroupData data;

    public Group(PlayerData owner, String name, List<PlayerData> memberNames) {
        data = new GroupData();
        this.data.ownerID = owner.playerID;
        this.data.name = name;
        for (PlayerData data : memberNames) {
            GroupMemberInfo info = new GroupMemberInfo();
            info.playerID = data.playerID;
            info.name = data.lastKnownName;
            this.data.members.add(info);
        }
    }

    public Group(GroupData data) {
        this.data = data;
    }

    public Group(String name, GroupMember owner) {
        this.data = new GroupData();
        this.data.name = name;
        this.owner = owner;
        this.data.ownerID = owner.getUniqueId();
    }

    public void addGroupMember(GroupMember member) {
        if (member == null || member.equals(owner) || data.ownerID.equals(member.getUniqueId())) {
            return;
        }
        GroupMemberInfo info = new GroupMemberInfo();
        info.playerID = member.getData().playerID;
        info.name = member.getData().lastKnownName;
        this.data.members.add(info);
    }

    public boolean addGroupMemberInfo(GroupMemberInfo info) {
        return info != null && !data.ownerID.equals(info.playerID) && !data.members.contains(info) && data.members.add(info);
    }

    public void removeGroupMember(GroupMember member) {
        if (member == null || member.equals(owner) || data.ownerID.equals(member.getUniqueId())) {
            return;
        }
        GroupMemberInfo info = new GroupMemberInfo();
        info.playerID = member.getData().playerID;
        info.name = member.getData().lastKnownName;
        this.data.members.remove(info);
    }

    public boolean removeGroupMemberInfo(GroupMemberInfo info) {
        return info != null && !info.playerID.equals(data.ownerID) && data.members.remove(info);
    }

    public boolean hasGroupMember(GroupMember member) {
        GroupMemberInfo info = new GroupMemberInfo();
        info.playerID = member.getData().playerID;
        info.name = member.getData().lastKnownName;
        return this.data.members.contains(info);
    }

    public boolean hasGroupMemberInfo(GroupMemberInfo info, boolean override) {
        if (info == null || data.ownerID.equals(info.playerID)) {
            return false;
        }
        for (GroupMemberInfo member : data.members) {
            if (member.playerID.equals(info.playerID)) {
                if (!member.name.equals(info.name) && override) {
                    member.name = info.name;
                }
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return this.data.getName();
    }

    public GroupMember getOwner() {
        return this.owner;
    }

    public List<GroupMemberInfo> getMembers() {
        return ImmutableList.copyOf(this.data.members);
    }

    public GroupData getData() {
        return this.data;
    }

    public GroupData getDataClone() {
        return this.data.clone();
    }
}
