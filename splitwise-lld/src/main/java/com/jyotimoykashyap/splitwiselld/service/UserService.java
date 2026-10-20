package com.jyotimoykashyap.splitwiselld.service;

import com.jyotimoykashyap.splitwiselld.models.User;

public interface UserService {
    User createUser(User user);
    User getUser(String id);
}
