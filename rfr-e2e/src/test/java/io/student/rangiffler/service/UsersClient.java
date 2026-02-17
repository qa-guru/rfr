package io.student.rangiffler.service;

import io.student.rangiffler.model.UserJson;

public interface UsersClient {

    UserJson createUser(String userName, String password);
    void deleteUserByUsername(String userName);
}
