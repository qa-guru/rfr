package io.student.rangiffler.data.extractor;

import io.student.rangiffler.data.entity.CountryEntity;
import io.student.rangiffler.data.entity.UserEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UserEntityExtractor implements ResultSetExtractor<List<UserEntity>> {

    public static final UserEntityExtractor instance = new UserEntityExtractor();

    private UserEntityExtractor() {
    }

    @Override
    public List<UserEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<UUID, UserEntity> userCache = new HashMap<>();

        while (rs.next()) {
            UUID userId = UUID.fromString(rs.getString("id"));

            UserEntity user = userCache.get(userId);
            if (user == null) {
                user = new UserEntity();
                user.setId(userId);
                user.setUsername(rs.getString("username"));
                user.setFirstname(rs.getString("firstname"));
                user.setLastName(rs.getString("lastName"));
                user.setAvatar(rs.getBytes("avatar"));

                String countryId = rs.getString("country_id");
                if (countryId != null) {
                    CountryEntity country = new CountryEntity();
                    country.setId(UUID.fromString(countryId));
                    user.setCountry(country);
                }

                userCache.put(userId, user);
            }
        }

        return new ArrayList<>(userCache.values());
    }
}
