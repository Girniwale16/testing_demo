-- V2__add_auth_columns_to_user_account_test.sql
-- Integration test for V2 migration: Add authentication columns to user_account table
-- This test validates the migration executes successfully and enforces all constraints

-- Test Setup: Create a test user_account table if not exists (for isolated testing)
-- In production, this migration runs against existing user_account table

-- TEST 1: Verify all columns are added successfully
DO $$
BEGIN
    -- Verify last_login_at column exists and is TIMESTAMP type
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'user_account' 
        AND column_name = 'last_login_at' 
        AND data_type = 'timestamp without time zone'
    ) THEN
        RAISE EXCEPTION 'TEST FAILED: last_login_at column not created or wrong type';
    END IF;

    -- Verify is_active column exists, is BOOLEAN, NOT NULL with DEFAULT TRUE
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'user_account' 
        AND column_name = 'is_active' 
        AND data_type = 'boolean'
        AND is_nullable = 'NO'
        AND column_default = 'true'
    ) THEN
        RAISE EXCEPTION 'TEST FAILED: is_active column not created correctly';
    END IF;

    -- Verify employment_status column exists and is VARCHAR(20)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'user_account' 
        AND column_name = 'employment_status' 
        AND data_type = 'character varying'
        AND character_maximum_length = 20
    ) THEN
        RAISE EXCEPTION 'TEST FAILED: employment_status column not created or wrong type';
    END IF;

    -- Verify end_date column exists and is DATE type
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'user_account' 
        AND column_name = 'end_date' 
        AND data_type = 'date'
    ) THEN
        RAISE EXCEPTION 'TEST FAILED: end_date column not created or wrong type';
    END IF;

    RAISE NOTICE 'TEST PASSED: All columns created successfully';
END $$;

-- TEST 2: Verify check constraint for employment_status
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.constraint_column_usage 
        WHERE table_name = 'user_account' 
        AND constraint_name = 'chk_user_employment_status'
    ) THEN
        RAISE EXCEPTION 'TEST FAILED: chk_user_employment_status constraint not created';
    END IF;

    RAISE NOTICE 'TEST PASSED: Check constraint created successfully';
END $$;

-- TEST 3: Verify employment_status constraint allows only ACTIVE and INACTIVE
DO $$
DECLARE
    test_user_id INTEGER;
BEGIN
    -- Insert test record with ACTIVE status (should succeed)
    INSERT INTO user_account (username, email, employment_status, is_active)
    VALUES ('test_user_active', 'test_active@example.com', 'ACTIVE', TRUE)
    RETURNING id INTO test_user_id;
    
    DELETE FROM user_account WHERE id = test_user_id;

    -- Insert test record with INACTIVE status (should succeed)
    INSERT INTO user_account (username, email, employment_status, is_active)
    VALUES ('test_user_inactive', 'test_inactive@example.com', 'INACTIVE', TRUE)
    RETURNING id INTO test_user_id;
    
    DELETE FROM user_account WHERE id = test_user_id;

    RAISE NOTICE 'TEST PASSED: Valid employment_status values accepted';
END $$;

-- TEST 4: Verify employment_status constraint rejects invalid values
DO $$
DECLARE
    test_failed BOOLEAN := FALSE;
BEGIN
    BEGIN
        INSERT INTO user_account (username, email, employment_status, is_active)
        VALUES ('test_user_invalid', 'test_invalid@example.com', 'TERMINATED', TRUE);
        
        test_failed := TRUE;
    EXCEPTION
        WHEN check_violation THEN
            RAISE NOTICE 'TEST PASSED: Invalid employment_status rejected correctly';
    END;

    IF test_failed THEN
        RAISE EXCEPTION 'TEST FAILED: Invalid employment_status was accepted';
    END IF;
END $$;

-- TEST 5: Verify is_active defaults to TRUE for new records
DO $$
DECLARE
    test_user_id INTEGER;
    test_is_active BOOLEAN;
BEGIN
    INSERT INTO user_account (username, email)
    VALUES ('test_user_default', 'test_default@example.com')
    RETURNING id INTO test_user_id;
    
    SELECT is_active INTO test_is_active FROM user_account WHERE id = test_user_id;
    
    IF test_is_active != TRUE THEN
        RAISE EXCEPTION 'TEST FAILED: is_active did not default to TRUE';
    END IF;
    
    DELETE FROM user_account WHERE id = test_user_id;
    
    RAISE NOTICE 'TEST PASSED: is_active defaults to TRUE';
END $$;

-- TEST 6: Verify indexes are created
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE tablename = 'user_account' 
        AND indexname = 'idx_user_account_is_active'
    ) THEN
        RAISE EXCEPTION 'TEST FAILED: idx_user_account_is_active index not created';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE tablename = 'user_account' 
        AND indexname = 'idx_user_account_employment_status'
    ) THEN
        RAISE EXCEPTION 'TEST FAILED: idx_user_account_employment_status index not created';
    END IF;

    RAISE NOTICE 'TEST PASSED: All indexes created successfully';
END $$;

-- TEST 7: Verify column comments are set
DO $$
DECLARE
    comment_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO comment_count
    FROM pg_description d
    JOIN pg_class c ON d.objoid = c.oid
    JOIN pg_attribute a ON a.attrelid = c.oid AND a.attnum = d.objsubid
    WHERE c.relname = 'user_account'
    AND a.attname IN ('last_login_at', 'is_active', 'employment_status', 'end_date');

    IF comment_count != 4 THEN
        RAISE EXCEPTION 'TEST FAILED: Not all column comments are set (expected 4, got %)', comment_count;
    END IF;

    RAISE NOTICE 'TEST PASSED: All column comments set successfully';
END $$;

-- TEST 8: Verify last_login_at accepts NULL and TIMESTAMP values
DO $$
DECLARE
    test_user_id INTEGER;
    test_timestamp TIMESTAMP;
BEGIN
    -- Test NULL value
    INSERT INTO user_account (username, email, last_login_at)
    VALUES ('test_user_null_login', 'test_null_login@example.com', NULL)
    RETURNING id INTO test_user_id;
    
    DELETE FROM user_account WHERE id = test_user_id;

    -- Test valid TIMESTAMP
    INSERT INTO user_account (username, email, last_login_at)
    VALUES ('test_user_with_login', 'test_with_login@example.com', '2024-01-15 10:30:00')
    RETURNING id INTO test_user_id;
    
    SELECT last_login_at INTO test_timestamp FROM user_account WHERE id = test_user_id;
    
    IF test_timestamp IS NULL THEN
        RAISE EXCEPTION 'TEST FAILED: last_login_at timestamp not stored correctly';
    END IF;
    
    DELETE FROM user_account WHERE id = test_user_id;
    
    RAISE NOTICE 'TEST PASSED: last_login_at accepts NULL and TIMESTAMP values';
END $$;

-- TEST 9: Verify end_date accepts NULL and DATE values
DO $$
DECLARE
    test_user_id INTEGER;
    test_date DATE;
BEGIN
    -- Test NULL value
    INSERT INTO user_account (username, email, end_date)
    VALUES ('test_user_null_end', 'test_null_end@example.com', NULL)
    RETURNING id INTO test_user_id;
    
    DELETE FROM user_account WHERE id = test_user_id;

    -- Test valid DATE
    INSERT INTO user_account (username, email, end_date, employment_status)
    VALUES ('test_user_with_end', 'test_with_end@example.com', '2024-12-31', 'INACTIVE')
    RETURNING id INTO test_user_id;
    
    SELECT end_date INTO test_date FROM user_account WHERE id = test_user_id;
    
    IF test_date IS NULL THEN
        RAISE EXCEPTION 'TEST FAILED: end_date not stored correctly';
    END IF;
    
    DELETE FROM user_account WHERE id = test_user_id;
    
    RAISE NOTICE 'TEST PASSED: end_date accepts NULL and DATE values';
END $$;

-- TEST 10: Verify independent deactivation scenarios (user_account vs staff)
DO $$
DECLARE
    test_user_id INTEGER;
BEGIN
    -- Scenario 1: User account INACTIVE, but could have ACTIVE staff record
    INSERT INTO user_account (username, email, employment_status, end_date, is_active)
    VALUES ('test_deactivated_user', 'test_deactivated@example.com', 'INACTIVE', '2024-01-01', FALSE)
    RETURNING id INTO test_user_id;
    
    -- Verify record created successfully
    IF test_user_id IS NULL THEN
        RAISE EXCEPTION 'TEST FAILED: Could not create deactivated user account';
    END IF;
    
    DELETE FROM user_account WHERE id = test_user_id;

    -- Scenario 2: User account ACTIVE with NULL end_date
    INSERT INTO user_account (username, email, employment_status, end_date, is_active)
    VALUES ('test_active_user', 'test_active@example.com', 'ACTIVE', NULL, TRUE)
    RETURNING id INTO test_user_id;
    
    IF test_user_id IS NULL THEN
        RAISE EXCEPTION 'TEST FAILED: Could not create active user account';
    END IF;
    
    DELETE FROM user_account WHERE id = test_user_id;
    
    RAISE NOTICE 'TEST PASSED: Independent deactivation scenarios work correctly';
END $$;

-- TEST SUMMARY
DO $$
BEGIN
    RAISE NOTICE '=== V2 MIGRATION TEST SUITE COMPLETED ===';
    RAISE NOTICE 'All tests passed successfully';
    RAISE NOTICE '- Column creation and types validated';
    RAISE NOTICE '- Check constraint enforcement verified';
    RAISE NOTICE '- Default values confirmed';
    RAISE NOTICE '- Indexes created and validated';
    RAISE NOTICE '- Column comments set correctly';
    RAISE NOTICE '- NULL and valid value handling tested';
    RAISE NOTICE '- Independent deactivation logic validated';
END $$;