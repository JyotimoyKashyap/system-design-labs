package com.jyotimoykashyap.splitwiselld.service;

import com.jyotimoykashyap.splitwiselld.models.Group;
import com.jyotimoykashyap.splitwiselld.models.User;
import com.jyotimoykashyap.splitwiselld.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;

    @Override
    public Group createGroup(Group group) {
        return groupRepository.save(group);
    }

    @Override
    public Group getGroup(String id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + id));
    }

    @Override
    public void addMember(String groupId, User user) {
        Group group = getGroup(groupId);
        group.addMember(user);
        groupRepository.save(group);
    }
}
