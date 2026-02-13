package io.student.rangiffler.data.extractor;

import io.student.rangiffler.data.entity.CountryEntity;
import io.student.rangiffler.data.entity.PhotoEntity;
import io.student.rangiffler.data.entity.UserEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class PhotoEntityExtractor implements ResultSetExtractor<List<PhotoEntity>> {

    public static final PhotoEntityExtractor instance = new PhotoEntityExtractor();

    private PhotoEntityExtractor() {
    }

    @Override
    public List<PhotoEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {

        List<PhotoEntity> photos = new ArrayList<>();

        while (rs.next()) {
            PhotoEntity p = new PhotoEntity();

            p.setId(UUID.fromString(rs.getString("id")));
            p.setDescription(rs.getString("description"));
            p.setPhoto(rs.getBytes("photo"));

            Timestamp ts = rs.getTimestamp("created_date");
            if (ts != null) {
                p.setCreatedDate(new Date(ts.getTime()));
            }

            UserEntity user = new UserEntity();
            user.setId(UUID.fromString(rs.getString("user_id")));
            p.setUser(user);

            CountryEntity country = new CountryEntity();
            country.setId(UUID.fromString(rs.getString("country_id")));
            p.setCountry(country);

            photos.add(p);
        }

        return photos;
    }
}
