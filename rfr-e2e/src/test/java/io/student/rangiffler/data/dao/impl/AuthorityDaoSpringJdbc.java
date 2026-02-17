package io.student.rangiffler.data.dao.impl;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.dao.AuthorityDao;
import io.student.rangiffler.data.entity.AuthorityEntity;
import io.student.rangiffler.data.tpl.DataSources;
import io.student.rangiffler.mapper.AuthorityRowMapper;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class AuthorityDaoSpringJdbc implements AuthorityDao {

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
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));

        jdbcTemplate.batchUpdate(SQL_CREATE_AUTHORITY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setObject(1, UUID.randomUUID().toString());
                ps.setString(2, authority[i].getUser().getId().toString());
                ps.setString(3, authority[i].getAuthority().name());
            }

            @Override
            public int getBatchSize() {
                return authority.length;
            }
        });

    }

    @Override
    public void deleteAuthorityByUserName(String userName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
        jdbcTemplate.update(SQL_DELETE_AUTHORITIES_BY_USERNAME, userName);
    }

    @Override
    public List<AuthorityEntity> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
        return jdbcTemplate.query(FIND_ALL_SQL, AuthorityRowMapper.INSTANCE);
    }
}
