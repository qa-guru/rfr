package io.student.rangiffler.data.repository.impl;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.entity.AuthUserEntity;
import io.student.rangiffler.data.entity.AuthorityEntity;
import io.student.rangiffler.data.extractor.AuthUserEntityExtractor;
import io.student.rangiffler.data.repository.AuthUserRepository;
import io.student.rangiffler.data.tpl.DataSources;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class AuthUserRepositorySpringJdbc implements AuthUserRepository {

    private static final Config CFG = Config.getInstance();
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final String SQL_CREATE_AUTHORITY =
            """
                    INSERT INTO `rangiffler-auth`.authority 
                    (id, user_id, authority)
                    VALUES (UUID_TO_BIN(?, true),UUID_TO_BIN(?, true),?);
            """;

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

    private final String SQL_DELETE_AUTHORITIES_BY_USERNAME =
            """
                DELETE a
                FROM `rangiffler-auth`.`authority` a
                JOIN `rangiffler-auth`.`user` u ON a.user_id = u.id
                WHERE u.username = ?;
            """;
    private static final String FIND_ALL_SQL =
            """
            SELECT 
                BIN_TO_UUID(u.id, true) AS user_id, u.username, u.password, u.enabled, u.account_non_expired,
                u.account_non_locked, u.credentials_non_expired, BIN_TO_UUID(a.id, true) AS authority_id, a.authority
            FROM `rangiffler-auth`.`user` u
            LEFT JOIN `rangiffler-auth`.authority a
                ON a.user_id = u.id
            """;


    private static final String FIND_BY_USERNAME_SQL =
            """
            SELECT
                BIN_TO_UUID(u.id, true)   AS id,
                u.username,
                u.password,
                u.enabled,
                u.account_non_expired,
                u.account_non_locked,
                u.credentials_non_expired,
                BIN_TO_UUID(a.id, true)   AS authority_id,
                a.authority
            FROM `rangiffler-auth`.`user` u
            JOIN `rangiffler-auth`.`authority` a ON u.id = a.user_id
            WHERE u.username = ?
            """;

    private final String SQL_UPDATE_USER =
            """
               UPDATE `rangiffler-auth`.`user`
               SET password = ?, enabled = ?, account_non_expired = ?, account_non_locked = ?, credentials_non_expired = ?
               WHERE id = UUID_TO_BIN(?, true);
            """;

    private static final String FIND_BY_ID_SQL =
            """
                SELECT 
                    BIN_TO_UUID(u.id, true) AS id,
                    u.username,
                    u.password,
                    u.enabled,
                    u.account_non_expired,
                    u.account_non_locked,
                    u.credentials_non_expired,
                    BIN_TO_UUID(a.id, true) AS authority_id,
                    a.authority
                FROM `rangiffler-auth`.`user` u
                LEFT JOIN `rangiffler-auth`.`authority` a ON u.id = a.user_id
                WHERE u.id = UUID_TO_BIN(?, true)
            """;

    @NotNull
    @Override
    public AuthUserEntity createUser(AuthUserEntity authUserEntity) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));

        if (authUserEntity.getId() == null) {
            authUserEntity.setId(UUID.randomUUID());
        }

        authUserEntity.setPassword(passwordEncoder.encode(authUserEntity.getPassword()));

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(SQL_CREATE_USER);
            ps.setString(1, authUserEntity.getId().toString());
            ps.setString(2, authUserEntity.getUsername());
            ps.setString(3, authUserEntity.getPassword());
            ps.setBoolean(4, authUserEntity.getEnabled());
            ps.setBoolean(5, authUserEntity.getAccountNonExpired());
            ps.setBoolean(6, authUserEntity.getAccountNonLocked());
            ps.setBoolean(7, authUserEntity.getCredentialsNonExpired());
            return ps;
        });

        if (authUserEntity.getAuthorities() != null) {
            for (AuthorityEntity authority : authUserEntity.getAuthorities()) {
                if (authority.getId() == null) {
                    authority.setId(UUID.randomUUID());
                }
                authority.setUser(authUserEntity);
                jdbcTemplate.update(con -> {
                    PreparedStatement ps = con.prepareStatement(SQL_CREATE_AUTHORITY);
                    ps.setString(1, authority.getId().toString());
                    ps.setString(2, authUserEntity.getId().toString());
                    ps.setString(3, authority.getAuthority().name());
                    return ps;
                });
            }
        }

        return authUserEntity;
    }

    @NotNull
    @Override
    public AuthUserEntity updateUser(AuthUserEntity user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));

        String encodedPassword = passwordEncoder.encode(user.getPassword());

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(SQL_UPDATE_USER);
            ps.setString(1, encodedPassword);
            ps.setBoolean(2, user.getEnabled());
            ps.setBoolean(3, user.getAccountNonExpired());
            ps.setBoolean(4, user.getAccountNonLocked());
            ps.setBoolean(5, user.getCredentialsNonExpired());
            ps.setString(6, user.getId().toString());
            return ps;
        });

        user.setPassword(encodedPassword);
        return user;
    }

    @Override
    public void deleteUserByUserName(String userName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));

        jdbcTemplate.update(SQL_DELETE_AUTHORITIES_BY_USERNAME, userName);
        jdbcTemplate.update(SQL_DELETE_USER_BY_USERNAME, userName);
    }

    @NotNull
    @Override
    public List<AuthUserEntity> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
        return jdbcTemplate.query(FIND_ALL_SQL, AuthUserEntityExtractor.instance);
    }

    @NotNull
    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
        JdbcTemplate jdbcTemplate =
                new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));

        List<AuthUserEntity> users = jdbcTemplate.query(
                FIND_BY_ID_SQL,
                AuthUserEntityExtractor.instance,
                id.toString()
        );

        return users.stream().findFirst();
    }

    @NotNull
    @Override
    public Optional<AuthUserEntity> findByUsername(String username) {
        JdbcTemplate jdbcTemplate =
                new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));

        AuthUserEntity user = Objects.requireNonNull(jdbcTemplate.query(
                FIND_BY_USERNAME_SQL,
                AuthUserEntityExtractor.instance,
                username
        )).getFirst();

        return Optional.ofNullable(user);
    }
}
