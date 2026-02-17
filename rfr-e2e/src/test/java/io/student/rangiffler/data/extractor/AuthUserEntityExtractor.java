package io.student.rangiffler.data.extractor;

import io.student.rangiffler.data.entity.AuthUserEntity;
import io.student.rangiffler.data.entity.Authority;
import io.student.rangiffler.data.entity.AuthorityEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class AuthUserEntityExtractor implements ResultSetExtractor<List<AuthUserEntity>> {

    public static final AuthUserEntityExtractor instance = new AuthUserEntityExtractor();
    private AuthUserEntityExtractor(){}

    @Override
    public List<AuthUserEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<UUID, AuthUserEntity> userCache = new HashMap<>();

        while (rs.next()) {
            UUID userId = UUID.fromString(rs.getString("id"));

            AuthUserEntity user = userCache.get(userId);
            if (user == null) {
                user = new AuthUserEntity();
                user.setId(userId);
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEnabled(rs.getBoolean("enabled"));
                user.setAccountNonExpired(rs.getBoolean("account_non_expired"));
                user.setAccountNonLocked(rs.getBoolean("account_non_locked"));
                user.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));

                userCache.put(userId, user);
            }

            AuthorityEntity authority = new AuthorityEntity();
            authority.setId(UUID.fromString(rs.getString("authority_id")));
            authority.setAuthority(Authority.valueOf(rs.getString("authority")));

            authority.setUser(user);
            user.getAuthorities().add(authority);
        }

        return new ArrayList<>(userCache.values());
    }
}
