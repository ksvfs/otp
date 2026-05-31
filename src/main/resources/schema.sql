CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(200) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    email VARCHAR(255),
    phone VARCHAR(50),
    telegram_chat_id VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS otp_config (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    code_length INTEGER NOT NULL CHECK (code_length BETWEEN 4 AND 10),
    ttl_seconds INTEGER NOT NULL CHECK (ttl_seconds BETWEEN 30 AND 3600),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO otp_config (id, code_length, ttl_seconds, updated_at)
VALUES (1, 6, 300, NOW())
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS otp_codes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    operation_id VARCHAR(255) NOT NULL,
    code_hash VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'EXPIRED', 'USED')),
    delivery_channels VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    used_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_otp_codes_user_operation ON otp_codes(user_id, operation_id);
CREATE INDEX IF NOT EXISTS idx_otp_codes_status ON otp_codes(status);
CREATE INDEX IF NOT EXISTS idx_otp_codes_expires_at ON otp_codes(expires_at);
