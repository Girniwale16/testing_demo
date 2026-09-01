-- VIS-2095: Update facility and user_account tables to align with Entity classes

-- Alter audit columns in facility table (facility.name already exists from V1)
ALTER TABLE facility ALTER COLUMN created_by TYPE VARCHAR(100) USING created_by::VARCHAR(100);
ALTER TABLE facility ALTER COLUMN created_by SET DEFAULT 'system';
UPDATE facility SET created_by = 'system' WHERE created_by IS NULL;
ALTER TABLE facility ALTER COLUMN created_by SET NOT NULL;
ALTER TABLE facility ALTER COLUMN updated_by TYPE VARCHAR(100) USING updated_by::VARCHAR(100);

-- Add NOT NULL constraints to created_at and updated_at columns in facility table
ALTER TABLE facility ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE facility ALTER COLUMN updated_at SET NOT NULL;

-- Alter audit columns in user_account table
ALTER TABLE user_account ALTER COLUMN created_by TYPE VARCHAR(100) USING created_by::VARCHAR(100);
ALTER TABLE user_account ALTER COLUMN created_by SET DEFAULT 'system';
UPDATE user_account SET created_by = 'system' WHERE created_by IS NULL;
ALTER TABLE user_account ALTER COLUMN created_by SET NOT NULL;
ALTER TABLE user_account ALTER COLUMN updated_by TYPE VARCHAR(100) USING updated_by::VARCHAR(100);

-- Add NOT NULL constraints to created_at and updated_at columns in user_account table
ALTER TABLE user_account ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE user_account ALTER COLUMN updated_at SET NOT NULL;

-- Columns required by the UserAccount entity but missing from V1/V2
ALTER TABLE user_account ADD COLUMN staff_member_id BIGINT;
ALTER TABLE user_account ADD COLUMN account_status VARCHAR(20);
ALTER TABLE user_account ADD COLUMN account_end_date DATE;

-- Add NOT NULL constraints to created_at and updated_at columns in staff table
ALTER TABLE staff ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE staff ALTER COLUMN updated_at SET NOT NULL;

-- Create indexes on created_at and updated_at for audit queries
CREATE INDEX idx_facility_created_at ON facility(created_at);
CREATE INDEX idx_facility_updated_at ON facility(updated_at);
CREATE INDEX idx_user_account_created_at ON user_account(created_at);
CREATE INDEX idx_user_account_updated_at ON user_account(updated_at);
CREATE INDEX idx_staff_created_at ON staff(created_at);
CREATE INDEX idx_staff_updated_at ON staff(updated_at);

-- No-op comment: Audit field constraints and indexes are ready without modifications