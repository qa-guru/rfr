package io.student.rangiffler.mapper;

import io.student.rangiffler.data.entity.AuthUserEntity;
import io.student.rangiffler.data.entity.Authority;
import io.student.rangiffler.data.entity.AuthorityEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AuthorityRowMapper implements RowMapper<AuthorityEntity> {

    public static final AuthorityRowMapper INSTANCE = new AuthorityRowMapper();

    private AuthorityRowMapper() {
    }


    @Override
    public AuthorityEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuthorityEntity ae = new AuthorityEntity();

        ae.setId(UUID.fromString(rs.getString("id")));
        ae.setAuthority(Authority.valueOf(rs.getString("authority")));

        AuthUserEntity user = new AuthUserEntity();
        user.setId(UUID.fromString(rs.getString("user_id")));
        ae.setUser(user);

        return ae;
    }
}
