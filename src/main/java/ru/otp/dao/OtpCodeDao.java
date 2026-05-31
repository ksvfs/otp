package ru.otp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.otp.model.DeliveryChannel;
import ru.otp.model.OtpCode;
import ru.otp.model.OtpStatus;

@Repository
public class OtpCodeDao {

    private final JdbcTemplate jdbcTemplate;

    public OtpCodeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public OtpCode create(long userId,
                          String operationId,
                          String codeHash,
                          List<DeliveryChannel> deliveryChannels,
                          Instant expiresAt) {
        Instant createdAt = Instant.now();
        String channelsValue = deliveryChannels.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        Long id = jdbcTemplate.queryForObject(
                """
                        INSERT INTO otp_codes (user_id, operation_id, code_hash, status, delivery_channels, expires_at, created_at)
                        VALUES (?, ?, ?, 'ACTIVE', ?, ?, ?)
                        RETURNING id
                        """,
                Long.class,
                userId,
                operationId,
                codeHash,
                channelsValue,
                Timestamp.from(expiresAt),
                Timestamp.from(createdAt)
        );

        return new OtpCode(id, userId, operationId, codeHash, OtpStatus.ACTIVE, deliveryChannels, expiresAt, createdAt, null);
    }

    public void expireActiveByUserAndOperation(long userId, String operationId) {
        jdbcTemplate.update(
                "UPDATE otp_codes SET status = 'EXPIRED' WHERE user_id = ? AND operation_id = ? AND status = 'ACTIVE'",
                userId,
                operationId
        );
    }

    public Optional<OtpCode> findLatestActive(long userId, String operationId) {
        List<OtpCode> codes = jdbcTemplate.query(
                """
                        SELECT id, user_id, operation_id, code_hash, status, delivery_channels, expires_at, created_at, used_at
                        FROM otp_codes
                        WHERE user_id = ? AND operation_id = ? AND status = 'ACTIVE'
                        ORDER BY created_at DESC
                        LIMIT 1
                        """,
                (resultSet, rowNum) -> mapCode(resultSet),
                userId,
                operationId
        );
        return codes.stream().findFirst();
    }

    public void markUsed(long id, Instant usedAt) {
        jdbcTemplate.update(
                "UPDATE otp_codes SET status = 'USED', used_at = ? WHERE id = ?",
                Timestamp.from(usedAt),
                id
        );
    }

    public void markExpired(long id) {
        jdbcTemplate.update("UPDATE otp_codes SET status = 'EXPIRED' WHERE id = ?", id);
    }

    public int expireOlderThan(Instant now) {
        return jdbcTemplate.update(
                "UPDATE otp_codes SET status = 'EXPIRED' WHERE status = 'ACTIVE' AND expires_at < ?",
                Timestamp.from(now)
        );
    }

    private OtpCode mapCode(ResultSet resultSet) throws SQLException {
        Timestamp usedAt = resultSet.getTimestamp("used_at");
        return new OtpCode(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getString("operation_id"),
                resultSet.getString("code_hash"),
                OtpStatus.valueOf(resultSet.getString("status")),
                parseChannels(resultSet.getString("delivery_channels")),
                resultSet.getTimestamp("expires_at").toInstant(),
                resultSet.getTimestamp("created_at").toInstant(),
                usedAt == null ? null : usedAt.toInstant()
        );
    }

    private List<DeliveryChannel> parseChannels(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .map(DeliveryChannel::valueOf)
                .toList();
    }
}
