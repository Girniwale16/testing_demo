-- V2: Add authentication columns to user_account table
-- This migration adds columns for authentication tracking and user account management

ALTER TABLE user_account
ADD COLUMN last_login_at TIMESTAMP,
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN employment_status VARCHAR(20),
ADD COLUMN end_date DATE;

-- Add check constraint for employment_status
ALTER TABLE user_account 
ADD CONSTRAINT chk_user_employment_status 
CHECK (employment_status IN ('ACTIVE', 'INACTIVE'));

-- Create indexes for performance
CREATE INDEX idx_user_account_is_active ON user_account(is_active);
CREATE INDEX idx_user_account_employment_status ON user_account(employment_status);

-- Column comments for documentation
COMMENT ON COLUMN user_account.last_login_at IS 'Timestamp of the user''s last successful login';
COMMENT ON COLUMN user_account.is_active IS 'Flag indicating whether the user account is active and can authenticate';
COMMENT ON COLUMN user_account.employment_status IS 'Employment status for user account deactivation (ACTIVE, INACTIVE) - separate from Staff entity employment_status';
COMMENT ON COLUMN user_account.end_date IS 'Date when user account was deactivated - parallel to Staff entity end_date but tracks user account lifecycle separately';

-- IMPORTANT: User account deactivation (employment_status, end_date on user_account table) 
-- is separate from Staff entity deactivation (employment_status, end_date on staff table).
-- UserAccount tracks authentication/login access, while Staff tracks employment records.
-- Both can be deactivated independently based on business requirements.

-- MIGRATION SAFETY: If this migration has already been deployed to production,
-- DO NOT modify this file. Instead, create a new migration file (V4, V5, etc.)
-- to prevent Flyway checksum validation failures.