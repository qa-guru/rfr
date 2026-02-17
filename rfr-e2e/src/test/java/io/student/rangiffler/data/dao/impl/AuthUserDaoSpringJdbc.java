package io.student.rangiffler.data.dao.impl;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.dao.AuthUserDao;
import io.student.rangiffler.data.entity.AuthUserEntity;
import io.student.rangiffler.data.tpl.DataSources;
import io.student.rangiffler.mapper.UserRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

public class AuthUserDaoSpringJdbc implements AuthUserDao {

    private static final Config CFG = Config.getInstance();
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    
    private final String SQL_CREATE_USER =
            """
                  INSERT INTO `rangiffler-auth`.`user`
                   (id, username,password,enabled,account_non_expired,account_non_locked,credentials_non_expired)  
                   VALUES (UUID_TO_BIN(?, true),?,?,?,?,?,?);
            """;

    private final String SQL_DELETE_USER_BY_USERNAME =
            """
                DELETE FROM `rangiffler-auth`.`user`
                WHERE username = ?;
            """;

    private static final String FIND_ALL_SQL =
            """
                SELECT BIN_TO_UUID(id, true) AS id,
                username, password,
                enabled, account_non_expired, account_non_locked, credentials_non_expired
                FROM `rangiffler-auth`.`user`
            """;
    @Override
    public AuthUserEntity createUser(AuthUserEntity authUserEntity) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
        String encodedPassword = passwordEncoder.encode(authUserEntity.getPassword());

        if (authUserEntity.getId() == null) {
            authUserEntity.setId(UUID.randomUUID());
        }

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    SQL_CREATE_USER,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, authUserEntity.getId().toString());
            ps.setString(2, authUserEntity.getUsername());
            ps.setString(3, encodedPassword);
            ps.setBoolean(4, authUserEntity.getEnabled());
            ps.setBoolean(5, authUserEntity.getAccountNonExpired());
            ps.setBoolean(6, authUserEntity.getAccountNonLocked());
            ps.setBoolean(7, authUserEntity.getCredentialsNonExpired());
            return ps;
        });

        return authUserEntity;
    }

    @Override
    public void deleteUserByUserName(String userName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
        jdbcTemplate.update(SQL_DELETE_USER_BY_USERNAME, userName);
    }

    @Override
    public List<AuthUserEntity> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
        return jdbcTemplate.query(FIND_ALL_SQL, UserRowMapper.INSTANCE);
    }
}
