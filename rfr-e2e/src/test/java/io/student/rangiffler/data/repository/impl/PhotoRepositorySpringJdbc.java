package io.student.rangiffler.data.repository.impl;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.entity.PhotoEntity;
import io.student.rangiffler.data.extractor.PhotoEntityExtractor;
import io.student.rangiffler.data.repository.PhotoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.student.rangiffler.data.tpl.DataSources;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.*;

public class PhotoRepositorySpringJdbc implements PhotoRepository {

    private static final Config CFG = Config.getInstance();

    private JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(DataSources.dataSource(CFG.apiJdbcUrl()));
    }

    private static final String SQL_INSERT_PHOTO =
            """
            INSERT INTO `rangiffler-api`.`photo`
                (id, user_id, country_id, description, photo, created_date)
            VALUES
                (UUID_TO_BIN(?, true), UUID_TO_BIN(?, true), UUID_TO_BIN(?, true), ?, ?, ?);
            """;

    private static final String SQL_UPDATE_PHOTO =
            """
            UPDATE `rangiffler-api`.`photo`
            SET country_id = UUID_TO_BIN(?, true),
                description = ?,
                photo = ?,
                created_date = ?
            WHERE id = UUID_TO_BIN(?, true);
            """;

    private static final String SQL_FIND_BY_ID =
            """
            SELECT
                BIN_TO_UUID(p.id, true)         AS id,
                BIN_TO_UUID(p.user_id, true)    AS user_id,
                BIN_TO_UUID(p.country_id, true) AS country_id,
                p.description,
                p.photo,
                p.created_date
            FROM `rangiffler-api`.`photo` p
            WHERE p.id = UUID_TO_BIN(?, true);
            """;

    private static final String SQL_FIND_BY_USERNAME_AND_DESCRIPTION =
            """
            SELECT
                BIN_TO_UUID(p.id, true)         AS id,
                BIN_TO_UUID(p.user_id, true)    AS user_id,
                BIN_TO_UUID(p.country_id, true) AS country_id,
                p.description,
                p.photo,
                p.created_date
            FROM `rangiffler-api`.`photo` p
            JOIN `rangiffler-api`.`user` u ON u.id = p.user_id
            WHERE u.username = ?
              AND p.description = ?;
            """;

    private static final String SQL_FIND_BY_USERNAME_AND_COUNTRY_CODE =
            """
            SELECT
                BIN_TO_UUID(p.id, true)         AS id,
                BIN_TO_UUID(p.user_id, true)    AS user_id,
                BIN_TO_UUID(p.country_id, true) AS country_id,
                p.description,
                p.photo,
                p.created_date
            FROM `rangiffler-api`.`photo` p
            JOIN `rangiffler-api`.`user` u ON u.id = p.user_id
            JOIN `rangiffler-api`.`country` c ON c.id = p.country_id
            WHERE u.username = ?
              AND c.code = ?;
            """;

    private static final String SQL_FIND_ALL_USER_PHOTOS =
            """
            SELECT
                BIN_TO_UUID(p.id, true)         AS id,
                BIN_TO_UUID(p.user_id, true)    AS user_id,
                BIN_TO_UUID(p.country_id, true) AS country_id,
                p.description,
                p.photo,
                p.created_date
            FROM `rangiffler-api`.`photo` p
            JOIN `rangiffler-api`.`user` u ON u.id = p.user_id
            WHERE u.username = ?
            ORDER BY p.created_date DESC;
            """;

    private static final String SQL_DELETE_PHOTO_BY_ID =
            """
            DELETE FROM `rangiffler-api`.`photo`
            WHERE id = UUID_TO_BIN(?, true);
            """;

    @NotNull
    @Override
    public PhotoEntity create(PhotoEntity photo) {
        if (photo.getId() == null) {
            photo.setId(UUID.randomUUID());
        }

        jdbcTemplate().update(con -> {
            PreparedStatement ps = con.prepareStatement(SQL_INSERT_PHOTO);
            ps.setString(1, photo.getId().toString());
            ps.setString(2, photo.getUser().getId().toString());
            ps.setString(3, photo.getCountry().getId().toString());
            ps.setString(4, photo.getDescription());
            ps.setBytes(5, photo.getPhoto());
            ps.setTimestamp(6, new Timestamp(photo.getCreatedDate().getTime()));
            return ps;
        });

        return photo;
    }

    @NotNull
    @Override
    public PhotoEntity update(PhotoEntity photo) {
        jdbcTemplate().update(con -> {
            PreparedStatement ps = con.prepareStatement(SQL_UPDATE_PHOTO);
            ps.setString(1, photo.getCountry().getId().toString());
            ps.setString(2, photo.getDescription());
            ps.setBytes(3, photo.getPhoto());
            ps.setTimestamp(4, new Timestamp(photo.getCreatedDate().getTime()));
            ps.setString(5, photo.getId().toString());
            return ps;
        });

        return photo;
    }

    @NotNull
    @Override
    public Optional<PhotoEntity> findById(UUID id) {
        List<PhotoEntity> result = jdbcTemplate().query(
                SQL_FIND_BY_ID,
                PhotoEntityExtractor.instance,
                id.toString()
        );

        return result.stream().findFirst();
    }

    @NotNull
    @Override
    public Optional<PhotoEntity> findByUsernameAndDescription(String username, String description) {
        List<PhotoEntity> result = jdbcTemplate().query(
                SQL_FIND_BY_USERNAME_AND_DESCRIPTION,
                PhotoEntityExtractor.instance,
                username,
                description
        );

        return result.stream().findFirst();
    }

    @NotNull
    @Override
    public Optional<PhotoEntity> findByUsernameAndCountry(String username, String code) {
        List<PhotoEntity> result = jdbcTemplate().query(
                SQL_FIND_BY_USERNAME_AND_COUNTRY_CODE,
                PhotoEntityExtractor.instance,
                username,
                code
        );

        return result.stream().findFirst();
    }

    @NotNull
    @Override
    public List<PhotoEntity> findAllUserPhoto(String username) {
        return jdbcTemplate().query(
                SQL_FIND_ALL_USER_PHOTOS,
                PhotoEntityExtractor.instance,
                username
        );
    }

    @Override
    public void remove(PhotoEntity photo) {
        jdbcTemplate().update(
                SQL_DELETE_PHOTO_BY_ID,
                photo.getId().toString()
        );
    }
}