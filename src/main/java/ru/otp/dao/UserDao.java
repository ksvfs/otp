package ru.otp.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.otp.model.Role;
import ru.otp.model.User;

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User create(String username,
                       String passwordHash,
                       Role role,
                       String email,
                       String phone,
                       String telegramChatId) {
        Instant now = Instant.now();
        Long id = jdbcTemplate.queryForObject(
                """
                        INSERT INTO users (username, password_hash, role, email, phone, telegram_chat_id, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        RETURNING id
                        """,
                Long.class,
                username,
                passwordHash,
                role.name(),
                email,
                phone,
                telegramChatId,
                Timestamp.from(now)
        );

        return new User(id, username, passwordHash, role, email, phone, telegramChatId, now);
    }

    public Optional<User> findByUsername(String username) {
        List<User> users = jdbcTemplate.query(
                "SELECT id, username, password_hash, role, email, phone, telegram_chat_id, created_at FROM users WHERE username = ?",
                userRowMapper(),
                username
        );
        return users.stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        List<User> users = jdbcTemplate.query(
                "SELECT id, username, password_hash, role, email, phone, telegram_chat_id, created_at FROM users WHERE id = ?",
                userRowMapper(),
                id
        );
        return users.stream().findFirst();
    }

    public boolean adminExists() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'",
                Integer.class
        );
        return count != null && count > 0;
    }

    public List<User> findAllNonAdmins() {
        return jdbcTemplate.query(
                "SELECT id, username, password_hash, role, email, phone, telegram_chat_id, created_at FROM users WHERE role <> 'ADMIN' ORDER BY id",
                userRowMapper()
        );
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    private RowMapper<User> userRowMapper() {
        return (resultSet, rowNum) -> mapUser(resultSet);
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                Role.valueOf(resultSet.getString("role")),
                resultSet.getString("email"),
                resultSet.getString("phone"),
                resultSet.getString("telegram_chat_id"),
                resultSet.getTimestamp("created_at").toInstant()
        );
    }
}
