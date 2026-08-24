ALTER TABLE user_account
ADD COLUMN last_login_at TIMESTAMP,
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_user_account_is_active ON user_account(is_active);

COMMENT ON COLUMN user_account.last_login_at IS 'Timestamp of the user''s last successful login';
COMMENT ON COLUMN user_account.is_active IS 'Flag indicating whether the user account is active and can authenticate';