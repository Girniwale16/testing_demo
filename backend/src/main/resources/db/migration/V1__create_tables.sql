-- VIS-2093: Create facility and user_account tables
-- Database: PostgreSQL
-- Note: Column types/lengths are proposed defaults inferred from ticket description.
--       Confirm against Section 8.5 of authoritative DB spec before marking story Done.

-- Create facility table
CREATE TABLE facility (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    timezone VARCHAR(100) NOT NULL,
    region_code VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT
);

-- Create user_account table
CREATE TABLE user_account (
    user_account_id BIGSERIAL PRIMARY KEY,
    facility_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('MANAGER', 'STAFF', 'SUPERVISOR')),
    staff_member_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    CONSTRAINT fk_user_account_facility FOREIGN KEY (facility_id) REFERENCES facility(id),
    CONSTRAINT uk_facility_username UNIQUE (facility_id, username)
);

-- Create indexes
CREATE INDEX idx_user_account_facility_role ON user_account(facility_id, role);
CREATE INDEX idx_user_account_staff_member ON user_account(staff_member_id);

-- Comments for documentation
COMMENT ON TABLE facility IS 'Stores facility information with timezone and region configuration';
COMMENT ON TABLE user_account IS 'Stores user accounts with facility-scoped username uniqueness';
COMMENT ON COLUMN user_account.password_hash IS 'Hash algorithm TBD per ticket - ensure sufficient length for chosen algorithm';
COMMENT ON COLUMN facility.timezone IS 'IANA timezone identifier - validation logic TBD per ticket';
COMMENT ON COLUMN user_account.staff_member_id IS 'Optional - maps STAFF role to staff_member_id per spec';

-- Create staff_member table
CREATE TABLE staff_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    contact VARCHAR(255) NOT NULL,
    role VARCHAR(100) NOT NULL,
    employment_status VARCHAR(50) NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    facility_id BIGINT NOT NULL,
    FOREIGN KEY (facility_id) REFERENCES facility(id) ON DELETE CASCADE
);

-- Create indexes for staff_member
CREATE INDEX idx_staff_facility ON staff_member(facility_id);
CREATE INDEX idx_staff_employment_status ON staff_member(employment_status);