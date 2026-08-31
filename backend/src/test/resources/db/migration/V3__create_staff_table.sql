-- V3__create_staff_table_test.sql
-- Comprehensive test suite for V3__create_staff_table migration
-- Tests table creation, constraints, indexes, and data integrity rules

-- Test 1: Verify staff table exists with correct structure
DO $$
BEGIN
    ASSERT (SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_name = 'staff'
    )), 'staff table should exist';
END $$;

-- Test 2: Verify all required columns exist with correct data types
DO $$
BEGIN
    ASSERT (SELECT COUNT(*) = 10 FROM information_schema.columns WHERE table_name = 'staff'), 
        'staff table should have exactly 10 columns';
    
    ASSERT (SELECT data_type = 'bigint' FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'id'), 
        'id column should be bigint';
    
    ASSERT (SELECT character_maximum_length = 100 FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'first_name'), 
        'first_name should be VARCHAR(100)';
    
    ASSERT (SELECT character_maximum_length = 100 FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'last_name'), 
        'last_name should be VARCHAR(100)';
    
    ASSERT (SELECT character_maximum_length = 255 FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'email'), 
        'email should be VARCHAR(255)';
    
    ASSERT (SELECT character_maximum_length = 50 FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'role'), 
        'role should be VARCHAR(50)';
    
    ASSERT (SELECT character_maximum_length = 20 FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'employment_status'), 
        'employment_status should be VARCHAR(20)';
END $$;

-- Test 3: Verify NOT NULL constraints
DO $$
BEGIN
    ASSERT (SELECT is_nullable = 'NO' FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'first_name'), 
        'first_name should be NOT NULL';
    
    ASSERT (SELECT is_nullable = 'NO' FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'last_name'), 
        'last_name should be NOT NULL';
    
    ASSERT (SELECT is_nullable = 'NO' FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'email'), 
        'email should be NOT NULL';
    
    ASSERT (SELECT is_nullable = 'NO' FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'role'), 
        'role should be NOT NULL';
    
    ASSERT (SELECT is_nullable = 'NO' FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'employment_status'), 
        'employment_status should be NOT NULL';
    
    ASSERT (SELECT is_nullable = 'NO' FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'facility_id'), 
        'facility_id should be NOT NULL';
END $$;

-- Test 4: Verify default values
DO $$
BEGIN
    ASSERT (SELECT column_default LIKE '%ACTIVE%' FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'employment_status'), 
        'employment_status should default to ACTIVE';
    
    ASSERT (SELECT column_default LIKE '%CURRENT_TIMESTAMP%' FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'created_at'), 
        'created_at should default to CURRENT_TIMESTAMP';
    
    ASSERT (SELECT column_default LIKE '%CURRENT_TIMESTAMP%' FROM information_schema.columns 
        WHERE table_name = 'staff' AND column_name = 'updated_at'), 
        'updated_at should default to CURRENT_TIMESTAMP';
END $$;

-- Test 5: Verify primary key constraint
DO $$
BEGIN
    ASSERT (SELECT EXISTS (
        SELECT FROM information_schema.table_constraints 
        WHERE table_name = 'staff' AND constraint_type = 'PRIMARY KEY'
    )), 'staff table should have a primary key';
END $$;

-- Test 6: Verify UNIQUE constraint on email
DO $$
BEGIN
    ASSERT (SELECT EXISTS (
        SELECT FROM information_schema.table_constraints tc
        JOIN information_schema.constraint_column_usage ccu 
            ON tc.constraint_name = ccu.constraint_name
        WHERE tc.table_name = 'staff' 
            AND tc.constraint_type = 'UNIQUE' 
            AND ccu.column_name = 'email'
    )), 'email column should have UNIQUE constraint';
END $$;

-- Test 7: Verify foreign key constraint to facility table
DO $$
BEGIN
    ASSERT (SELECT EXISTS (
        SELECT FROM information_schema.table_constraints 
        WHERE table_name = 'staff' 
            AND constraint_name = 'fk_staff_facility' 
            AND constraint_type = 'FOREIGN KEY'
    )), 'fk_staff_facility constraint should exist';
END $$;

-- Test 8: Verify check constraint for employment_status values
DO $$
BEGIN
    ASSERT (SELECT EXISTS (
        SELECT FROM information_schema.check_constraints 
        WHERE constraint_name = 'chk_employment_status'
    )), 'chk_employment_status constraint should exist';
END $$;

-- Test 9: Verify check constraint for end_date logic
DO $$
BEGIN
    ASSERT (SELECT EXISTS (
        SELECT FROM information_schema.check_constraints 
        WHERE constraint_name = 'chk_end_date_inactive'
    )), 'chk_end_date_inactive constraint should exist';
END $$;

-- Test 10: Verify all required indexes exist
DO $$
BEGIN
    ASSERT (SELECT EXISTS (
        SELECT FROM pg_indexes 
        WHERE tablename = 'staff' AND indexname = 'idx_staff_facility_id'
    )), 'idx_staff_facility_id index should exist';
    
    ASSERT (SELECT EXISTS (
        SELECT FROM pg_indexes 
        WHERE tablename = 'staff' AND indexname = 'idx_staff_employment_status'
    )), 'idx_staff_employment_status index should exist';
    
    ASSERT (SELECT EXISTS (
        SELECT FROM pg_indexes 
        WHERE tablename = 'staff' AND indexname = 'idx_staff_facility_status'
    )), 'idx_staff_facility_status index should exist';
END $$;

-- Test 11: Insert valid ACTIVE staff member (should succeed)
DO $$
DECLARE
    test_facility_id BIGINT;
BEGIN
    -- Create test facility first
    INSERT INTO facility (name, address, city, state, zip_code, phone, created_at, updated_at)
    VALUES ('Test Facility', '123 Test St', 'Test City', 'TS', '12345', '555-0100', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING id INTO test_facility_id;
    
    -- Insert active staff
    INSERT INTO staff (first_name, last_name, email, role, employment_status, facility_id)
    VALUES ('John', 'Doe', 'john.doe@test.com', 'Nurse', 'ACTIVE', test_facility_id);
    
    ASSERT (SELECT COUNT(*) = 1 FROM staff WHERE email = 'john.doe@test.com'), 
        'Active staff member should be inserted successfully';
    
    -- Cleanup
    DELETE FROM staff WHERE email = 'john.doe@test.com';
    DELETE FROM facility WHERE id = test_facility_id;
END $$;

-- Test 12: Insert valid INACTIVE staff member with end_date (should succeed)
DO $$
DECLARE
    test_facility_id BIGINT;
BEGIN
    INSERT INTO facility (name, address, city, state, zip_code, phone, created_at, updated_at)
    VALUES ('Test Facility 2', '456 Test Ave', 'Test City', 'TS', '12345', '555-0101', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING id INTO test_facility_id;
    
    INSERT INTO staff (first_name, last_name, email, role, employment_status, end_date, facility_id)
    VALUES ('Jane', 'Smith', 'jane.smith@test.com', 'Doctor', 'INACTIVE', '2024-01-01', test_facility_id);
    
    ASSERT (SELECT COUNT(*) = 1 FROM staff WHERE email = 'jane.smith@test.com'), 
        'Inactive staff member with end_date should be inserted successfully';
    
    DELETE FROM staff WHERE email = 'jane.smith@test.com';
    DELETE FROM facility WHERE id = test_facility_id;
END $$;

-- Test 13: Attempt to insert ACTIVE staff with end_date (should fail)
DO $$
DECLARE
    test_facility_id BIGINT;
    insert_failed BOOLEAN := FALSE;
BEGIN
    INSERT INTO facility (name, address, city, state, zip_code, phone, created_at, updated_at)
    VALUES ('Test Facility 3', '789 Test Blvd', 'Test City', 'TS', '12345', '555-0102', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING id INTO test_facility_id;
    
    BEGIN
        INSERT INTO staff (first_name, last_name, email, role, employment_status, end_date, facility_id)
        VALUES ('Invalid', 'User', 'invalid@test.com', 'Admin', 'ACTIVE', '2024-01-01', test_facility_id);
    EXCEPTION
        WHEN check_violation THEN
            insert_failed := TRUE;
    END;
    
    ASSERT insert_failed, 'ACTIVE staff with end_date should violate check constraint';
    
    DELETE FROM facility WHERE id = test_facility_id;
END $$;

-- Test 14: Attempt to insert INACTIVE staff without end_date (should fail)
DO $$
DECLARE
    test_facility_id BIGINT;
    insert_failed BOOLEAN := FALSE;
BEGIN
    INSERT INTO facility (name, address, city, state, zip_code, phone, created_at, updated_at)
    VALUES ('Test Facility 4', '321 Test Ln', 'Test City', 'TS', '12345', '555-0103', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING id INTO test_facility_id;
    
    BEGIN
        INSERT INTO staff (first_name, last_name, email, role, employment_status, facility_id)
        VALUES ('Invalid', 'User2', 'invalid2@test.com', 'Nurse', 'INACTIVE', test_facility_id);
    EXCEPTION
        WHEN check_violation THEN
            insert_failed := TRUE;
    END;
    
    ASSERT insert_failed, 'INACTIVE staff without end_date should violate check constraint';
    
    DELETE FROM facility WHERE id = test_facility_id;
END $$;

-- Test 15: Attempt to insert staff with invalid employment_status (should fail)
DO $$
DECLARE
    test_facility_id BIGINT;
    insert_failed BOOLEAN := FALSE;
BEGIN
    INSERT INTO facility (name, address, city, state, zip_code, phone, created_at, updated_at)
    VALUES ('Test Facility 5', '654 Test Dr', 'Test City', 'TS', '12345', '555-0104', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING id INTO test_facility_id;
    
    BEGIN
        INSERT INTO staff (first_name, last_name, email, role, employment_status, facility_id)
        VALUES ('Invalid', 'User3', 'invalid3@test.com', 'Doctor', 'PENDING', test_facility_id);
    EXCEPTION
        WHEN check_violation THEN
            insert_failed := TRUE;
    END;
    
    ASSERT insert_failed, 'Invalid employment_status should violate check constraint';
    
    DELETE FROM facility WHERE id = test_facility_id;
END $$;

-- Test 16: Attempt to insert duplicate email (should fail)
DO $$
DECLARE
    test_facility_id BIGINT;
    insert_failed BOOLEAN := FALSE;
BEGIN
    INSERT INTO facility (name, address, city, state, zip_code, phone, created_at, updated_at)
    VALUES ('Test Facility 6', '987 Test Ct', 'Test City', 'TS', '12345', '555-0105', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING id INTO test_facility_id;
    
    INSERT INTO staff (first_name, last_name, email, role, employment_status, facility_id)
    VALUES ('First', 'User', 'duplicate@test.com', 'Nurse', 'ACTIVE', test_facility_id);
    
    BEGIN
        INSERT INTO staff (first_name, last_name, email, role, employment_status, facility_id)
        VALUES ('Second', 'User', 'duplicate@test.com', 'Doctor', 'ACTIVE', test_facility_id);
    EXCEPTION
        WHEN unique_violation THEN
            insert_failed := TRUE;
    END;
    
    ASSERT insert_failed, 'Duplicate email should violate UNIQUE constraint';
    
    DELETE FROM staff WHERE email = 'duplicate@test.com';
    DELETE FROM facility WHERE id = test_facility_id;
END $$;

-- Test 17: Attempt to insert staff with non-existent facility_id (should fail)
DO $$
DECLARE
    insert_failed BOOLEAN := FALSE;
BEGIN
    BEGIN
        INSERT INTO staff (first_name, last_name, email, role, employment_status, facility_id)
        VALUES ('Invalid', 'User4', 'invalid4@test.com', 'Admin', 'ACTIVE', 999999);
    EXCEPTION
        WHEN foreign_key_violation THEN
            insert_failed := TRUE;
    END;
    
    ASSERT insert_failed, 'Non-existent facility_id should violate foreign key constraint';
END $$;

-- Test 18: Attempt to delete facility with associated staff (should fail due to ON DELETE RESTRICT)
DO $$
DECLARE
    test_facility_id BIGINT;
    delete_failed BOOLEAN := FALSE;
BEGIN
    INSERT INTO facility (name, address, city, state, zip_code, phone, created_at, updated_at)
    VALUES ('Test Facility 7', '147 Test Way', 'Test City', 'TS', '12345', '555-0106', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING id INTO test_facility_id;
    
    INSERT INTO staff (first_name, last_name, email, role, employment_status, facility_id)
    VALUES ('Protected', 'User', 'protected@test.com', 'Nurse', 'ACTIVE', test_facility_id);
    
    BEGIN
        DELETE FROM facility WHERE id = test_facility_id;
    EXCEPTION
        WHEN foreign_key_violation THEN
            delete_failed := TRUE;
    END;
    
    ASSERT delete_failed, 'Deleting facility with staff should fail due to ON DELETE RESTRICT';
    
    DELETE FROM staff WHERE email = 'protected@test.com';
    DELETE FROM facility WHERE id = test_facility_id;
END $$;

-- Test 19: Verify default timestamp values are set correctly
DO $$
DECLARE
    test_facility_id BIGINT;
    test_created_at TIMESTAMP;
    test_updated_at TIMESTAMP;
BEGIN
    INSERT INTO facility (name, address, city, state, zip_code, phone, created_at, updated_at)
    VALUES ('Test Facility 8', '258 Test Pl', 'Test City', 'TS', '12345', '555-0107', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING id INTO test_facility_id;
    
    INSERT INTO staff (first_name, last_name, email, role, facility_id)
    VALUES ('Timestamp', 'Test', 'timestamp@test.com', 'Nurse', test_facility_id);
    
    SELECT created_at, updated_at INTO test_created_at, test_updated_at
    FROM staff WHERE email = 'timestamp@test.com';
    
    ASSERT test_created_at IS NOT NULL, 'created_at should be automatically set';
    ASSERT test_updated_at IS NOT NULL, 'updated_at should be automatically set';
    ASSERT test_created_at <= CURRENT_TIMESTAMP, 'created_at should not be in the future';
    ASSERT test_updated_at <= CURRENT_TIMESTAMP, 'updated_at should not be in the future';
    
    DELETE FROM staff WHERE email = 'timestamp@test.com';
    DELETE FROM facility WHERE id = test_facility_id;
END $$;

-- Test 20: Verify composite index covers facility_id and employment_status
DO $$
BEGIN
    ASSERT (SELECT COUNT(*) = 1 FROM pg_indexes 
        WHERE tablename = 'staff' 
            AND indexname = 'idx_staff_facility_status'
            AND indexdef LIKE '%facility_id%employment_status%'), 
        'Composite index should include both facility_id and employment_status';
END $$;