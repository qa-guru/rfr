package io.student.rangiffler.data.dao.impl;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.dao.AuthUserDao;
import io.student.rangiffler.data.entity.AuthUserEntity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.student.rangiffler.data.tpl.Connections.holder;

public class AuthUserDaoJdbc implements AuthUserDao {

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
        String encodedPassword = passwordEncoder.encode(authUserEntity.getPassword());

        if (authUserEntity.getId() == null) {
            authUserEntity.setId(UUID.randomUUID());
        }

        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(SQL_CREATE_USER)) {
            ps.setString(1, authUserEntity.getId().toString());
            ps.setString(2, authUserEntity.getUsername());
            ps.setString(3, encodedPassword);
            ps.setBoolean(4, authUserEntity.getEnabled());
            ps.setBoolean(5, authUserEntity.getAccountNonExpired());
            ps.setBoolean(6, authUserEntity.getAccountNonLocked());
            ps.setBoolean(7, authUserEntity.getCredentialsNonExpired());

            ps.executeUpdate();
            return authUserEntity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
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

    @Override
    public List<AuthUserEntity> findAll() {
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(FIND_ALL_SQL)) {
            List<AuthUserEntity> users = new ArrayList<>();
            var rs = ps.executeQuery();

            while (rs.next()) {
                users.add(buildUser(rs));
            }

            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all users", e);
        }
    }

    private AuthUserEntity buildUser(ResultSet rs) throws SQLException {
        AuthUserEntity user = new AuthUserEntity();
        user.setId(UUID.fromString(rs.getString("id")));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEnabled(rs.getBoolean("enabled"));
        user.setAccountNonExpired(rs.getBoolean("account_non_expired"));
        user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
        user.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
        return user;
    }
}
