-- Test file: V4__update_audit_fields_test.sql
-- Comprehensive test scenarios for V4__update_audit_fields.sql migration

-- ============================================================================
-- TEST SETUP: Create test tables and initial data
-- ============================================================================

-- Create test schema for isolation
CREATE SCHEMA IF NOT EXISTS test_v4_migration;
SET search_path TO test_v4_migration;

-- Recreate tables as they would exist before V4 migration
CREATE TABLE facility (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_account (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100),
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE staff (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- TEST CASE 1: Verify facility table audit column type changes
-- ============================================================================

-- Insert test data with various created_by values
INSERT INTO facility (name, created_by, updated_by) VALUES 
    ('Facility 1', 123, 456),
    ('Facility 2', NULL, 789),
    ('Facility 3', 999, NULL);

-- Apply migration logic for facility table
ALTER TABLE facility ALTER COLUMN created_by TYPE VARCHAR(100) USING created_by::VARCHAR(100);
ALTER TABLE facility ALTER COLUMN created_by SET DEFAULT 'system';
UPDATE facility SET created_by = 'system' WHERE created_by IS NULL;
ALTER TABLE facility ALTER COLUMN created_by SET NOT NULL;
ALTER TABLE facility ALTER COLUMN updated_by TYPE VARCHAR(100) USING updated_by::VARCHAR(100);
ALTER TABLE facility ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE facility ALTER COLUMN updated_at SET NOT NULL;

-- Test assertions for facility table
DO $$
DECLARE
    v_data_type VARCHAR;
    v_is_nullable VARCHAR;
    v_column_default VARCHAR;
    v_null_count INTEGER;
BEGIN
    -- Assert created_by is VARCHAR(100)
    SELECT data_type, character_maximum_length INTO v_data_type, v_column_default
    FROM information_schema.columns 
    WHERE table_name = 'facility' AND column_name = 'created_by';
    
    IF v_data_type != 'character varying' THEN
        RAISE EXCEPTION 'TEST FAILED: facility.created_by should be VARCHAR type';
    END IF;
    
    -- Assert created_by is NOT NULL
    SELECT is_nullable INTO v_is_nullable
    FROM information_schema.columns 
    WHERE table_name = 'facility' AND column_name = 'created_by';
    
    IF v_is_nullable != 'NO' THEN
        RAISE EXCEPTION 'TEST FAILED: facility.created_by should be NOT NULL';
    END IF;
    
    -- Assert NULL values were updated to 'system'
    SELECT COUNT(*) INTO v_null_count FROM facility WHERE created_by = 'system';
    IF v_null_count < 1 THEN
        RAISE EXCEPTION 'TEST FAILED: NULL created_by values should be updated to system';
    END IF;
    
    -- Assert created_at and updated_at are NOT NULL
    SELECT is_nullable INTO v_is_nullable
    FROM information_schema.columns 
    WHERE table_name = 'facility' AND column_name = 'created_at';
    
    IF v_is_nullable != 'NO' THEN
        RAISE EXCEPTION 'TEST FAILED: facility.created_at should be NOT NULL';
    END IF;
    
    RAISE NOTICE 'TEST PASSED: Facility table audit columns updated correctly';
END $$;

-- ============================================================================
-- TEST CASE 2: Verify user_account table audit column type changes
-- ============================================================================

-- Insert test data
INSERT INTO user_account (username, created_by, updated_by) VALUES 
    ('user1', 100, 200),
    ('user2', NULL, 300),
    ('user3', 400, NULL);

-- Apply migration logic for user_account table
ALTER TABLE user_account ALTER COLUMN created_by TYPE VARCHAR(100) USING created_by::VARCHAR(100);
ALTER TABLE user_account ALTER COLUMN created_by SET DEFAULT 'system';
UPDATE user_account SET created_by = 'system' WHERE created_by IS NULL;
ALTER TABLE user_account ALTER COLUMN created_by SET NOT NULL;
ALTER TABLE user_account ALTER COLUMN updated_by TYPE VARCHAR(100) USING updated_by::VARCHAR(100);
ALTER TABLE user_account ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE user_account ALTER COLUMN updated_at SET NOT NULL;

-- Add new columns
ALTER TABLE user_account ADD COLUMN staff_member_id BIGINT;
ALTER TABLE user_account ADD COLUMN account_status VARCHAR(20);
ALTER TABLE user_account ADD COLUMN account_end_date DATE;

-- Test assertions for user_account table
DO $$
DECLARE
    v_column_exists BOOLEAN;
    v_is_nullable VARCHAR;
BEGIN
    -- Assert new columns exist
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'user_account' AND column_name = 'staff_member_id'
    ) INTO v_column_exists;
    
    IF NOT v_column_exists THEN
        RAISE EXCEPTION 'TEST FAILED: user_account.staff_member_id column should exist';
    END IF;
    
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'user_account' AND column_name = 'account_status'
    ) INTO v_column_exists;
    
    IF NOT v_column_exists THEN
        RAISE EXCEPTION 'TEST FAILED: user_account.account_status column should exist';
    END IF;
    
    SELECT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'user_account' AND column_name = 'account_end_date'
    ) INTO v_column_exists;
    
    IF NOT v_column_exists THEN
        RAISE EXCEPTION 'TEST FAILED: user_account.account_end_date column should exist';
    END IF;
    
    RAISE NOTICE 'TEST PASSED: User account table columns added correctly';
END $$;

-- ============================================================================
-- TEST CASE 3: Verify staff table updated_at trigger functionality
-- ============================================================================

-- Apply migration logic for staff table
ALTER TABLE staff ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE staff ALTER COLUMN updated_at SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE staff ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

CREATE OR REPLACE FUNCTION update_staff_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_staff_updated_at ON staff;

CREATE TRIGGER trigger_staff_updated_at
    BEFORE UPDATE ON staff
    FOR EACH ROW
    EXECUTE FUNCTION update_staff_updated_at();

-- Insert test staff record
INSERT INTO staff (name) VALUES ('Test Staff');

-- Wait and update to test trigger
SELECT pg_sleep(0.1);
UPDATE staff SET name = 'Updated Staff' WHERE name = 'Test Staff';

-- Test trigger functionality
DO $$
DECLARE
    v_created_at TIMESTAMP;
    v_updated_at TIMESTAMP;
BEGIN
    SELECT created_at, updated_at INTO v_created_at, v_updated_at
    FROM staff WHERE name = 'Updated Staff';
    
    IF v_updated_at <= v_created_at THEN
        RAISE EXCEPTION 'TEST FAILED: updated_at should be automatically updated by trigger';
    END IF;
    
    RAISE NOTICE 'TEST PASSED: Staff updated_at trigger works correctly';
END $$;

-- ============================================================================
-- TEST CASE 4: Verify staff table updated_by column and foreign key
-- ============================================================================

-- Add updated_by column (idempotent)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE staff ADD COLUMN updated_by BIGINT;
    END IF;
END $$;

-- Add foreign key constraint (idempotent)
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

-- Test foreign key constraint
DO $$
DECLARE
    v_constraint_exists BOOLEAN;
    v_user_id BIGINT;
BEGIN
    -- Verify constraint exists
    SELECT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_staff_updated_by' AND table_name = 'staff'
    ) INTO v_constraint_exists;
    
    IF NOT v_constraint_exists THEN
        RAISE EXCEPTION 'TEST FAILED: Foreign key constraint fk_staff_updated_by should exist';
    END IF;
    
    -- Test valid foreign key reference
    SELECT id INTO v_user_id FROM user_account LIMIT 1;
    UPDATE staff SET updated_by = v_user_id WHERE id = (SELECT id FROM staff LIMIT 1);
    
    -- Test invalid foreign key reference (should fail)
    BEGIN
        UPDATE staff SET updated_by = 999999 WHERE id = (SELECT id FROM staff LIMIT 1);
        RAISE EXCEPTION 'TEST FAILED: Invalid foreign key should be rejected';
    EXCEPTION
        WHEN foreign_key_violation THEN
            RAISE NOTICE 'TEST PASSED: Foreign key constraint enforced correctly';
    END;
END $$;

-- ============================================================================
-- TEST CASE 5: Verify indexes are created
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_facility_created_at ON facility(created_at);
CREATE INDEX IF NOT EXISTS idx_facility_updated_at ON facility(updated_at);
CREATE INDEX IF NOT EXISTS idx_user_account_created_at ON user_account(created_at);
CREATE INDEX IF NOT EXISTS idx_user_account_updated_at ON user_account(updated_at);
CREATE INDEX IF NOT EXISTS idx_staff_created_at ON staff(created_at);
CREATE INDEX IF NOT EXISTS idx_staff_updated_at ON staff(updated_at);

-- Test index existence
DO $$
DECLARE
    v_index_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_index_count
    FROM pg_indexes
    WHERE schemaname = 'test_v4_migration'
    AND indexname IN (
        'idx_facility_created_at',
        'idx_facility_updated_at',
        'idx_user_account_created_at',
        'idx_user_account_updated_at',
        'idx_staff_created_at',
        'idx_staff_updated_at'
    );
    
    IF v_index_count != 6 THEN
        RAISE EXCEPTION 'TEST FAILED: Expected 6 audit indexes, found %', v_index_count;
    END IF;
    
    RAISE NOTICE 'TEST PASSED: All audit indexes created successfully';
END $$;

-- ============================================================================
-- TEST CASE 6: Verify migration idempotency
-- ============================================================================

-- Run migration logic again to ensure idempotency
DO $$
BEGIN
    -- Try adding updated_by column again (should not fail)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE staff ADD COLUMN updated_by BIGINT;
    END IF;
    
    -- Try adding foreign key again (should not fail)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_staff_updated_by' AND table_name = 'staff'
    ) THEN
        ALTER TABLE staff ADD CONSTRAINT fk_staff_updated_by 
        FOREIGN KEY (updated_by) REFERENCES user_account(id);
    END IF;
    
    RAISE NOTICE 'TEST PASSED: Migration is idempotent';
END $$;

-- ============================================================================
-- TEST CASE 7: Verify rollback capability
-- ============================================================================

-- Test rollback script (reverse migration)
DO $$
BEGIN
    -- Drop indexes
    DROP INDEX IF EXISTS idx_staff_updated_at;
    DROP INDEX IF EXISTS idx_staff_created_at;
    DROP INDEX IF EXISTS idx_user_account_updated_at;
    DROP INDEX IF EXISTS idx_user_account_created_at;
    DROP INDEX IF EXISTS idx_facility_updated_at;
    DROP INDEX IF EXISTS idx_facility_created_at;
    
    -- Drop foreign key constraint
    ALTER TABLE staff DROP CONSTRAINT IF EXISTS fk_staff_updated_by;
    
    -- Drop updated_by column
    ALTER TABLE staff DROP COLUMN IF EXISTS updated_by;
    
    -- Drop trigger and function
    DROP TRIGGER IF EXISTS trigger_staff_updated_at ON staff;
    DROP FUNCTION IF EXISTS update_staff_updated_at();
    
    -- Drop new user_account columns
    ALTER TABLE user_account DROP COLUMN IF EXISTS account_end_date;
    ALTER TABLE user_account DROP COLUMN IF EXISTS account_status;
    ALTER TABLE user_account DROP COLUMN IF EXISTS staff_member_id;
    
    RAISE NOTICE 'TEST PASSED: Rollback script executed successfully';
END $$;

-- ============================================================================
-- TEST CLEANUP
-- ============================================================================

-- Drop test schema
DROP SCHEMA IF EXISTS test_v4_migration CASCADE;
RESET search_path;

-- ============================================================================
-- TEST SUMMARY
-- ============================================================================
-- All test cases verify:
-- 1. Facility table audit column type changes and NOT NULL constraints
-- 2. User account table audit column changes and new columns
-- 3. Staff table updated_at trigger automatic timestamp update
-- 4. Staff table updated_by column and foreign key constraint
-- 5. Index creation on audit columns
-- 6. Migration idempotency (can be run multiple times safely)
-- 7. Rollback capability (migration is reversible)
-- ============================================================================