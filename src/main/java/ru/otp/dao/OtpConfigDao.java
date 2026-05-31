package ru.otp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.otp.model.OtpConfig;

@Repository
public class OtpConfigDao {

    private final JdbcTemplate jdbcTemplate;

    public OtpConfigDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public OtpConfig getCurrent() {
        List<OtpConfig> configs = jdbcTemplate.query(
                "SELECT id, code_length, ttl_seconds, updated_at FROM otp_config WHERE id = 1",
                (resultSet, rowNum) -> mapConfig(resultSet)
        );
        return configs.getFirst();
    }

    public OtpConfig update(int codeLength, int ttlSeconds) {
        Instant now = Instant.now();
        jdbcTemplate.update(
                "UPDATE otp_config SET code_length = ?, ttl_seconds = ?, updated_at = ? WHERE id = 1",
                codeLength,
                ttlSeconds,
                Timestamp.from(now)
        );
        return new OtpConfig(1, codeLength, ttlSeconds, now);
    }

    private OtpConfig mapConfig(ResultSet resultSet) throws SQLException {
        return new OtpConfig(
                resultSet.getInt("id"),
                resultSet.getInt("code_length"),
                resultSet.getInt("ttl_seconds"),
                resultSet.getTimestamp("updated_at").toInstant()
        );
    }
}
