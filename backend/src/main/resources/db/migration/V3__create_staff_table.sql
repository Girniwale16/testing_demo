-- V3__create_staff_table.sql
-- Migration to create staff table with employment tracking and facility relationship

-- Create staff table
CREATE TABLE IF NOT EXISTS staff (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    employment_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    end_date DATE,
    facility_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Add foreign key constraint to facility table
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_staff_facility'
    ) THEN
        ALTER TABLE staff ADD CONSTRAINT fk_staff_facility 
            FOREIGN KEY (facility_id) REFERENCES facility(id) ON DELETE RESTRICT;
    END IF;
END $$;

-- Add index on active column for performance
CREATE INDEX IF NOT EXISTS idx_staff_active ON staff(active);

-- Add index on facility_id for query performance
CREATE INDEX IF NOT EXISTS idx_staff_facility_id ON staff(facility_id);

-- Add composite index on facility_id and active for filtered queries
CREATE INDEX IF NOT EXISTS idx_staff_facility_active ON staff(facility_id, active);

-- Add index on employment_status for active staff queries
CREATE INDEX IF NOT EXISTS idx_staff_employment_status ON staff(employment_status);

-- Add composite index for facility-scoped active staff queries
CREATE INDEX IF NOT EXISTS idx_staff_facility_status ON staff(facility_id, employment_status);

-- Add check constraint to ensure employment_status is either 'ACTIVE' or 'INACTIVE'
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_employment_status'
    ) THEN
        ALTER TABLE staff ADD CONSTRAINT chk_employment_status 
            CHECK (employment_status IN ('ACTIVE', 'INACTIVE'));
    END IF;
END $$;

-- Add check constraint to ensure end_date is only set when employment_status is 'INACTIVE'
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_end_date_inactive'
    ) THEN
        ALTER TABLE staff ADD CONSTRAINT chk_end_date_inactive 
            CHECK ((employment_status = 'INACTIVE' AND end_date IS NOT NULL) OR (employment_status = 'ACTIVE' AND end_date IS NULL));
    END IF;
END $$;