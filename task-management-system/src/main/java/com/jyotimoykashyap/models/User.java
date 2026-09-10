package com.jyotimoykashyap.models;

import java.util.UUID;

public class User {
    private String username;
    private UUID id;

    public User(String username) throws Exception {
        id = UUID.randomUUID();
        updateUserName(username);
    }

    public void updateUserName(String username) throws Exception{
        validateUserName(username);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return username;
    }

    private void validateUserName(String username) throws Exception {
        if (username.isBlank())
            throw new IllegalArgumentException("Blank username is not allowed");
    }
}
