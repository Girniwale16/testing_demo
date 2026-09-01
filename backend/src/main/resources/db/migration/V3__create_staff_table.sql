-- V3__create_staff_table.sql
-- Migration to create staff table with employment tracking and facility relationship

-- Create staff table
CREATE TABLE staff (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    facility_name VARCHAR(255) NOT NULL,
    is_deactivated BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Add index on is_deactivated for efficient filtering
CREATE INDEX idx_staff_deactivated ON staff(is_deactivated);

-- Add index on facility_name for facility-based queries
CREATE INDEX idx_staff_facility ON staff(facility_name);

-- Staff table schema is ready for Staff entity mapping without modifications