package io.student.rangiffler.data.dao;

import io.student.rangiffler.data.entity.AuthUserEntity;

import java.util.List;

public interface AuthUserDao {
    AuthUserEntity createUser(AuthUserEntity user);
    void deleteUserByUserName(String userName);
    List<AuthUserEntity> findAll();
}
