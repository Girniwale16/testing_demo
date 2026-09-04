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
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    roles VARCHAR(255) NOT NULL,
    facility_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_user_account_facility FOREIGN KEY (facility_id) REFERENCES facility(id),
    CONSTRAINT uk_facility_username UNIQUE (facility_id, username)
);

-- Create audit_log table for storing audit events
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    user_id BIGINT,
    username VARCHAR(100),
    facility_id BIGINT,
    action VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_log_user FOREIGN KEY (user_id) REFERENCES user_account(id),
    CONSTRAINT fk_audit_log_facility FOREIGN KEY (facility_id) REFERENCES facility(id)
);

-- Create indexes
CREATE INDEX idx_user_account_facility_role ON user_account(facility_id, roles);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_user ON audit_log(user_id);
CREATE INDEX idx_audit_log_facility ON audit_log(facility_id);

-- Comments for documentation
COMMENT ON TABLE facility IS 'Stores facility information with name, address, timezone and region configuration';
COMMENT ON TABLE user_account IS 'Stores user accounts with facility-scoped username uniqueness for authentication and authorization';
COMMENT ON TABLE audit_log IS 'Stores audit trail events for compliance and security monitoring';
COMMENT ON COLUMN user_account.password_hash IS 'Hash algorithm TBD per ticket - ensure sufficient length for chosen algorithm';
COMMENT ON COLUMN facility.timezone IS 'IANA timezone identifier - validation logic TBD per ticket';
COMMENT ON COLUMN facility.active IS 'Active status flag for facility';
COMMENT ON COLUMN user_account.roles IS 'User roles for authorization (e.g., MANAGER, STAFF, SUPERVISOR)';
COMMENT ON COLUMN audit_log.event_type IS 'Type of audit event (e.g., LOGIN, LOGOUT, CREATE, UPDATE, DELETE)';
COMMENT ON COLUMN audit_log.action IS 'Action performed (e.g., INSERT, UPDATE, DELETE, ACCESS)';