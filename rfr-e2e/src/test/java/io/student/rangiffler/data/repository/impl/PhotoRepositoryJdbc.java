package io.student.rangiffler.data.repository.impl;

import io.student.rangiffler.config.Config;
import io.student.rangiffler.data.entity.CountryEntity;
import io.student.rangiffler.data.entity.PhotoEntity;
import io.student.rangiffler.data.entity.UserEntity;
import io.student.rangiffler.data.repository.PhotoRepository;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import static io.student.rangiffler.data.tpl.Connections.holder;

public class PhotoRepositoryJdbc implements PhotoRepository {

    private static final Config CFG = Config.getInstance();

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

        try (PreparedStatement ps =
                     holder(CFG.apiJdbcUrl()).connection().prepareStatement(SQL_INSERT_PHOTO)) {

            ps.setString(1, photo.getId().toString());
            ps.setString(2, photo.getUser().getId().toString());
            ps.setString(3, photo.getCountry().getId().toString());
            ps.setString(4, photo.getDescription());
            ps.setBytes(5, photo.getPhoto());
            ps.setTimestamp(6, new Timestamp(photo.getCreatedDate().getTime()));

            ps.executeUpdate();
            return photo;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create photo: id=" + photo.getId(), e);
        }
    }

    @NotNull
    @Override
    public PhotoEntity update(PhotoEntity photo) {
        try (PreparedStatement ps =
                     holder(CFG.apiJdbcUrl()).connection().prepareStatement(SQL_UPDATE_PHOTO)) {

            ps.setString(1, photo.getCountry().getId().toString());
            ps.setString(2, photo.getDescription());
            ps.setBytes(3, photo.getPhoto());
            ps.setTimestamp(4, new Timestamp(photo.getCreatedDate().getTime()));
            ps.setString(5, photo.getId().toString());

            ps.executeUpdate();
            return photo;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update photo: id=" + photo.getId(), e);
        }
    }

    @NotNull
    @Override
    public Optional<PhotoEntity> findById(UUID id) {
        try (PreparedStatement ps =
                     holder(CFG.apiJdbcUrl()).connection().prepareStatement(SQL_FIND_BY_ID)) {

            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(buildPhoto(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find photo by id=" + id, e);
        }
    }

    @NotNull
    @Override
    public Optional<PhotoEntity> findByUsernameAndDescription(String username, String description) {
        try (PreparedStatement ps =
                     holder(CFG.apiJdbcUrl()).connection().prepareStatement(SQL_FIND_BY_USERNAME_AND_DESCRIPTION)) {

            ps.setString(1, username);
            ps.setString(2, description);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(buildPhoto(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find photo by username/description", e);
        }
    }

    @NotNull
    @Override
    public Optional<PhotoEntity> findByUsernameAndCountry(String username, String code) {
        try (PreparedStatement ps =
                     holder(CFG.apiJdbcUrl()).connection().prepareStatement(SQL_FIND_BY_USERNAME_AND_COUNTRY_CODE)) {

            ps.setString(1, username);
            ps.setString(2, code);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(buildPhoto(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find photo by username/country", e);
        }
    }

    @NotNull
    @Override
    public List<PhotoEntity> findAllUserPhoto(String username) {
        try (PreparedStatement ps =
                     holder(CFG.apiJdbcUrl()).connection().prepareStatement(SQL_FIND_ALL_USER_PHOTOS)) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            List<PhotoEntity> photos = new ArrayList<>();

            while (rs.next()) {
                photos.add(buildPhoto(rs));
            }

            return photos;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all photos for username=" + username, e);
        }
    }

    @Override
    public void remove(PhotoEntity photo) {
        try (PreparedStatement ps =
                     holder(CFG.apiJdbcUrl()).connection().prepareStatement(SQL_DELETE_PHOTO_BY_ID)) {

            ps.setString(1, photo.getId().toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete photo: id=" + photo.getId(), e);
        }
    }

    private PhotoEntity buildPhoto(ResultSet rs) throws SQLException {
        PhotoEntity p = new PhotoEntity();

        p.setId(UUID.fromString(rs.getString("id")));
        p.setDescription(rs.getString("description"));
        p.setPhoto(rs.getBytes("photo"));

        Timestamp ts = rs.getTimestamp("created_date");
        if (ts != null) {
            p.setCreatedDate(new Date(ts.getTime()));
        }

        UserEntity u = new UserEntity();
        u.setId(UUID.fromString(rs.getString("user_id")));
        p.setUser(u);

        CountryEntity c = new CountryEntity();
        c.setId(UUID.fromString(rs.getString("country_id")));
        p.setCountry(c);

        return p;
    }
}

