package io.student.rangiffler.data.repository.impl;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.entity.AuthUserEntity;
import io.student.rangiffler.data.entity.Authority;
import io.student.rangiffler.data.entity.AuthorityEntity;
import io.student.rangiffler.data.repository.AuthUserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static io.student.rangiffler.data.tpl.Connections.holder;

public class AuthUserRepositoryJdbc implements AuthUserRepository {

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
        String encodedPassword = passwordEncoder.encode(authUserEntity.getPassword());

        if (authUserEntity.getId() == null) {
            authUserEntity.setId(UUID.randomUUID());
        }

        try (PreparedStatement userPs = holder(CFG.authJdbcUrl()).connection().prepareStatement(SQL_CREATE_USER);
            PreparedStatement authPs = holder(CFG.authJdbcUrl()).connection().prepareStatement(SQL_CREATE_AUTHORITY)
        ) {
            userPs.setString(1, authUserEntity.getId().toString());
            userPs.setString(2, authUserEntity.getUsername());
            userPs.setString(3, encodedPassword);
            userPs.setBoolean(4, authUserEntity.getEnabled());
            userPs.setBoolean(5, authUserEntity.getAccountNonExpired());
            userPs.setBoolean(6, authUserEntity.getAccountNonLocked());
            userPs.setBoolean(7, authUserEntity.getCredentialsNonExpired());

            userPs.executeUpdate();

            for (AuthorityEntity a : authUserEntity.getAuthorities()) {
                authPs.setObject(1, UUID.randomUUID().toString());
                authPs.setString(2, a.getUser().getId().toString());
                authPs.setString(3, a.getAuthority().name());
                authPs.addBatch();
                authPs.clearParameters();
            }
            authPs.executeBatch();

            return authUserEntity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    @NotNull
    @Override
    public AuthUserEntity updateUser(AuthUserEntity user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());

        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(SQL_UPDATE_USER)) {
            ps.setString(1, encodedPassword);
            ps.setBoolean(2, user.getEnabled());
            ps.setBoolean(3, user.getAccountNonExpired());
            ps.setBoolean(4, user.getAccountNonLocked());
            ps.setBoolean(5, user.getCredentialsNonExpired());
            ps.setString(6, user.getId().toString());

            ps.executeUpdate();
            user.setPassword(encodedPassword);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public void deleteUserByUserName(String userName) {
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(SQL_DELETE_USER_BY_USERNAME)) {
            ps.setString(1, userName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user by userName = " + userName, e);
        }
    }

    @NotNull
    @Override
    public List<AuthUserEntity> findAll() {
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(FIND_ALL_SQL)) {
            Map<UUID, AuthUserEntity> users = new LinkedHashMap<>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID userId = UUID.fromString(rs.getString("user_id"));
                AuthUserEntity user = users.computeIfAbsent(userId, id -> {
                    try {
                        return buildUser(rs);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                String authorityName = rs.getString("authority");
                if (authorityName != null) {
                    AuthorityEntity authority = new AuthorityEntity();
                    authority.setId(UUID.fromString(rs.getString("authority_id")));
                    authority.setAuthority(Authority.valueOf(authorityName));
                    authority.setUser(user);
                    user.getAuthorities().add(authority);
                }
            }

            return new ArrayList<>(users.values());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all users", e);
        }
    }


    @NotNull
    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(FIND_BY_ID_SQL)) {

            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();

            AuthUserEntity user = null;

            while (rs.next()) {
                if (user == null) {
                    user = new AuthUserEntity();
                    user.setId(UUID.fromString(rs.getString("id")));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setEnabled(rs.getBoolean("enabled"));
                    user.setAccountNonExpired(rs.getBoolean("account_non_expired"));
                    user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
                    user.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
                }

                String authorityName = rs.getString("authority");
                if (authorityName != null) {
                    AuthorityEntity authority = new AuthorityEntity();
                    authority.setId(UUID.fromString(rs.getString("authority_id")));
                    authority.setAuthority(Authority.valueOf(authorityName));
                    authority.setUser(user);
                    user.getAuthorities().add(authority);
                }
            }

            return Optional.ofNullable(user);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id = " + id, e);
        }
    }

    @NotNull
    @Override
    public Optional<AuthUserEntity> findByUsername(String username) {
        try (PreparedStatement ps =
                     holder(CFG.authJdbcUrl()).connection().prepareStatement(FIND_BY_USERNAME_SQL)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            AuthUserEntity user = null;

            while (rs.next()) {
                if (user == null) {
                    user = new AuthUserEntity();
                    user.setId(UUID.fromString(rs.getString("id")));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setEnabled(rs.getBoolean("enabled"));
                    user.setAccountNonExpired(rs.getBoolean("account_non_expired"));
                    user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
                    user.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
                }

                String authorityName = rs.getString("authority");
                if (authorityName != null) {
                    AuthorityEntity authority = new AuthorityEntity();
                    authority.setId(UUID.fromString(rs.getString("authority_id")));
                    authority.setAuthority(Authority.valueOf(authorityName));
                    authority.setUser(user);
                    user.getAuthorities().add(authority);
                }
            }

            return Optional.ofNullable(user);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by username = " + username, e);
        }
    }

    private AuthUserEntity buildUser(ResultSet rs) throws SQLException {
        AuthUserEntity user = new AuthUserEntity();
        user.setId(UUID.fromString(rs.getString("user_id")));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEnabled(rs.getBoolean("enabled"));
        user.setAccountNonExpired(rs.getBoolean("account_non_expired"));
        user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
        user.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
        return user;
    }


}
