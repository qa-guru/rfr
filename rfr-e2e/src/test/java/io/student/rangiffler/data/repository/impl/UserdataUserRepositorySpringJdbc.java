package io.student.rangiffler.data.repository.impl;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.entity.FriendshipStatus;
import io.student.rangiffler.data.entity.UserEntity;
import io.student.rangiffler.data.extractor.UserEntityExtractor;
import io.student.rangiffler.data.repository.UserdataUserRepository;
import io.student.rangiffler.data.tpl.DataSources;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserdataUserRepositorySpringJdbc implements UserdataUserRepository {

    private static final Config CFG = Config.getInstance();

    private static final String SQL_FIND_USER_BY_ID =
            """
            SELECT
                BIN_TO_UUID(id, true) AS id,
                username,
                firstname,
                lastName,
                avatar,
                BIN_TO_UUID(country_id, true) AS country_id
            FROM `rangiffler-api`.`user`
            WHERE id = UUID_TO_BIN(?, true);
            """;

    private static final String SQL_INSERT_FRIENDSHIP =
            """
            INSERT INTO `rangiffler-api`.`friendship`
            (requester_id, addressee_id, created_date, status)
            VALUES (UUID_TO_BIN(?, true), UUID_TO_BIN(?, true), ?, ?);
            """;

    private static final String SQL_UPDATE_FRIENDSHIP_STATUS =
            """
            UPDATE `rangiffler-api`.`friendship`
            SET status = ?
            WHERE requester_id = UUID_TO_BIN(?, true)
              AND addressee_id = UUID_TO_BIN(?, true);
            """;

    private static final String SQL_INSERT_USER =
            """
            INSERT INTO `rangiffler-api`.`user`
            (id, username, firstname, lastName, avatar, country_id)
            VALUES (UUID_TO_BIN(?, true), ?, ?, ?, ?, UUID_TO_BIN(?, true));
            """;

    private static final String SQL_UPDATE_USER =
            """
            UPDATE `rangiffler-api`.`user`
            SET username = ?, firstname = ?, lastName = ?, avatar = ?, country_id = UUID_TO_BIN(?, true)
            WHERE id = UUID_TO_BIN(?, true);
            """;

    private static final String SQL_FIND_USER_BY_USERNAME =
            """
            SELECT
                BIN_TO_UUID(id, true) AS id,
                username,
                firstname,
                lastName,
                avatar,
                BIN_TO_UUID(country_id, true) AS country_id
            FROM `rangiffler-api`.`user`
            WHERE username = ?;
            """;

    private static final String SQL_FIND_ALL_USERS =
            """
            SELECT
                BIN_TO_UUID(id, true) AS id,
                username,
                firstname,
                lastName,
                avatar,
                BIN_TO_UUID(country_id, true) AS country_id
            FROM `rangiffler-api`.`user`;
            """;


    @NotNull
    @Override
    public UserEntity create(UserEntity user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.apiJdbcUrl()));

        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(SQL_INSERT_USER);
            ps.setString(1, user.getId().toString());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getFirstname());
            ps.setString(4, user.getLastName());
            ps.setBytes(5, user.getAvatar());
            ps.setString(6, user.getCountry().getId().toString());
            return ps;
        });

        return user;
    }

    @Override
    public UserEntity update(UserEntity user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.apiJdbcUrl()));

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(SQL_UPDATE_USER);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getFirstname());
            ps.setString(3, user.getLastName());
            ps.setBytes(4, user.getAvatar());
            ps.setString(5, user.getCountry().getId().toString());
            ps.setString(6, user.getId().toString());
            return ps;
        });

        return user;
    }

    @NotNull
    @Override
    public Optional<UserEntity> findById(UUID id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.apiJdbcUrl()));

        List<UserEntity> users = jdbcTemplate.query(
                SQL_FIND_USER_BY_ID,
                UserEntityExtractor.instance,
                id.toString()
        );

        return users.stream().findFirst();
    }

    @Override
    public void addIncomeInvitation(UserEntity requester, UserEntity addressee) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.apiJdbcUrl()));

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(SQL_INSERT_FRIENDSHIP);
            ps.setString(1, requester.getId().toString());
            ps.setString(2, addressee.getId().toString());
            ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
            ps.setString(4, FriendshipStatus.PENDING.name());
            return ps;
        });
    }

    @Override
    public void addOutcomeInvitation(UserEntity requester, UserEntity addressee) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.apiJdbcUrl()));

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(SQL_INSERT_FRIENDSHIP);
            ps.setString(1, requester.getId().toString());
            ps.setString(2, addressee.getId().toString());
            ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
            ps.setString(4, FriendshipStatus.PENDING.name());
            return ps;
        });
    }

    @Override
    public void addFriend(UserEntity requester, UserEntity addressee) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.apiJdbcUrl()));

        int updated1 = jdbcTemplate.update(SQL_UPDATE_FRIENDSHIP_STATUS,
                FriendshipStatus.ACCEPTED.name(),
                requester.getId().toString(),
                addressee.getId().toString()
        );

        int updated2 = jdbcTemplate.update(SQL_UPDATE_FRIENDSHIP_STATUS,
                FriendshipStatus.ACCEPTED.name(),
                addressee.getId().toString(),
                requester.getId().toString()
        );

        if (updated2 == 0) {
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(SQL_INSERT_FRIENDSHIP);
                ps.setString(1, addressee.getId().toString());
                ps.setString(2, requester.getId().toString());
                ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
                ps.setString(4, FriendshipStatus.ACCEPTED.name());
                return ps;
            });
        }
    }

    @NotNull
    @Override
    public Optional<UserEntity> findByUsername(String username) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.apiJdbcUrl()));

        List<UserEntity> users = jdbcTemplate.query(
                SQL_FIND_USER_BY_USERNAME,
                UserEntityExtractor.instance,
                username
        );

        return users.stream().findFirst();
    }

    @NotNull
    @Override
    public List<UserEntity> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.apiJdbcUrl()));
        return jdbcTemplate.query(SQL_FIND_ALL_USERS, UserEntityExtractor.instance);
    }
}
