package io.student.rangiffler.data.dao;

import io.student.rangiffler.data.entity.AuthorityEntity;

import java.util.List;

public interface AuthorityDao{
    void createAuthority(AuthorityEntity... authority);
    void deleteAuthorityByUserName(String username);
    List<AuthorityEntity> findAll();
}
