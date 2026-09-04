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

-- Verify staff table has updated_at column with automatic timestamp update trigger
-- PostgreSQL does not support ON UPDATE CURRENT_TIMESTAMP, so we ensure updated_at exists and create a trigger
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE staff ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

-- Create or replace trigger function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_staff_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop trigger if exists to ensure idempotency
DROP TRIGGER IF EXISTS trigger_staff_updated_at ON staff;

-- Create trigger to automatically update updated_at on staff table modifications
CREATE TRIGGER trigger_staff_updated_at
    BEFORE UPDATE ON staff
    FOR EACH ROW
    EXECUTE FUNCTION update_staff_updated_at();

-- Add updated_by column to staff table if it doesn't exist (idempotent)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE staff ADD COLUMN updated_by BIGINT;
    END IF;
END $$;

-- Add foreign key constraint for updated_by referencing user_account(id) (idempotent)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_staff_updated_by' AND table_name = 'staff'
    ) THEN
        ALTER TABLE staff ADD CONSTRAINT fk_staff_updated_by 
        FOREIGN KEY (updated_by) REFERENCES user_account(id);
    END IF;
END $$;

-- Create indexes on created_at and updated_at for audit queries (idempotent)
CREATE INDEX IF NOT EXISTS idx_facility_created_at ON facility(created_at);
CREATE INDEX IF NOT EXISTS idx_facility_updated_at ON facility(updated_at);
CREATE INDEX IF NOT EXISTS idx_user_account_created_at ON user_account(created_at);
CREATE INDEX IF NOT EXISTS idx_user_account_updated_at ON user_account(updated_at);
CREATE INDEX IF NOT EXISTS idx_staff_created_at ON staff(created_at);
CREATE INDEX IF NOT EXISTS idx_staff_updated_at ON staff(updated_at);

-- Audit field constraints and indexes are ready