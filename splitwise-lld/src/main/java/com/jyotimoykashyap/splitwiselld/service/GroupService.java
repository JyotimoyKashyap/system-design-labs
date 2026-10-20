package com.jyotimoykashyap.splitwiselld.service;

import com.jyotimoykashyap.splitwiselld.models.Group;
import com.jyotimoykashyap.splitwiselld.models.User;

public interface GroupService {
    Group createGroup(Group group);
    Group getGroup(String id);
    void addMember(String groupId, User user);
}
