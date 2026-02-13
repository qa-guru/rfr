package io.student.rangiffler.data.dao.impl;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.dao.AuthorityDao;
import io.student.rangiffler.data.entity.AuthUserEntity;
import io.student.rangiffler.data.entity.Authority;
import io.student.rangiffler.data.entity.AuthorityEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.student.rangiffler.data.tpl.Connections.holder;

public class AuthorityDaoJdbc implements AuthorityDao {

    private static final Config CFG = Config.getInstance();

    private final String SQL_CREATE_AUTHORITY =
            """
                    INSERT INTO `rangiffler-auth`.authority 
                    (id, user_id, authority)
                    VALUES (UUID_TO_BIN(?, true),UUID_TO_BIN(?, true),?);
            """;

    private final String SQL_DELETE_AUTHORITIES_BY_USERNAME =
            """
                DELETE a FROM `rangiffler-auth`.authority a
                JOIN `rangiffler-auth`.user u ON a.user_id = u.id
                WHERE u.username = ?;
            """;

    private static final String FIND_ALL_SQL =
            """
               SELECT BIN_TO_UUID(a.id, true)     AS id,
               BIN_TO_UUID(a.user_id, true) AS user_id,
               a.authority                  AS authority
               FROM `rangiffler-auth`.authority a
            """;

    @Override
    public void createAuthority(AuthorityEntity... authority) {
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(SQL_CREATE_AUTHORITY)) {
            for (AuthorityEntity a : authority) {
                ps.setObject(1, UUID.randomUUID().toString());
                ps.setString(2, a.getUser().getId().toString());
                ps.setString(3, a.getAuthority().name());
                ps.addBatch();
                ps.clearParameters();
            }
            ps.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create authority", e);
        }
    }

    @Override
    public void deleteAuthorityByUserName(String userName) {
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(SQL_DELETE_AUTHORITIES_BY_USERNAME)) {
            ps.setString(1, userName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete authority by userName = " + userName, e);
        }
    }

    @Override
    public List<AuthorityEntity> findAll() {
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(FIND_ALL_SQL)) {
            List<AuthorityEntity> authorities = new ArrayList<>();
            var rs = ps.executeQuery();

            while (rs.next()) {
                authorities.add(buildAuthority(rs));
            }

            return authorities;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all authorities", e);
        }
    }

    private AuthorityEntity buildAuthority(ResultSet rs) throws SQLException {
        AuthorityEntity ae = new AuthorityEntity();
        ae.setId(UUID.fromString(rs.getString("id")));
        ae.setAuthority(Authority.valueOf(rs.getString("authority")));

        AuthUserEntity user = new AuthUserEntity();
        user.setId(UUID.fromString(rs.getString("user_id")));
        ae.setUser(user);

        return ae;
    }
}
