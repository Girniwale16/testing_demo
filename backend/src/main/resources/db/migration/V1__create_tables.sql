-- VIS-2093: Create facility and user_account tables
-- Database: PostgreSQL
-- Note: Column types/lengths are proposed defaults inferred from ticket description.
--       Confirm against Section 8.5 of authoritative DB spec before marking story Done.

-- Create facility table
CREATE TABLE facility (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    timezone VARCHAR(100) NOT NULL,
    region_code VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- Create user_account table
CREATE TABLE user_account (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(100) NOT NULL,
    facility_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_user_account_facility FOREIGN KEY (facility_id) REFERENCES facility(id),
    CONSTRAINT uk_facility_username UNIQUE (facility_id, username)
);

-- Create indexes for query performance
CREATE INDEX idx_user_account_email ON user_account(email);
CREATE INDEX idx_user_account_role ON user_account(role);
CREATE INDEX idx_user_account_facility_role ON user_account(facility_id, role);

-- Comments for documentation
COMMENT ON TABLE facility IS 'Stores facility information with name, address, timezone and region configuration';
COMMENT ON TABLE user_account IS 'Stores user accounts with facility-scoped username uniqueness for authentication and authorization';
COMMENT ON COLUMN user_account.password_hash IS 'Hash algorithm TBD per ticket - ensure sufficient length for chosen algorithm';
COMMENT ON COLUMN user_account.first_name IS 'Staff member first name';
COMMENT ON COLUMN user_account.last_name IS 'Staff member last name';
COMMENT ON COLUMN user_account.email IS 'Staff member email address - indexed for query performance';
COMMENT ON COLUMN user_account.role IS 'User role for authorization (e.g., MANAGER, STAFF, SUPERVISOR) - indexed for query performance';
COMMENT ON COLUMN facility.timezone IS 'IANA timezone identifier - validation logic TBD per ticket';
COMMENT ON COLUMN facility.active IS 'Active status flag for facility';