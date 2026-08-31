-- VIS-2095: Update facility and user_account tables to align with Entity classes
-- Add name column to facility
ALTER TABLE facility ADD COLUMN name VARCHAR(200) NOT NULL DEFAULT 'Default Facility';

-- Alter audit columns in facility table
ALTER TABLE facility ALTER COLUMN created_by TYPE VARCHAR(100);
ALTER TABLE facility ALTER COLUMN created_by SET DEFAULT 'system';
UPDATE facility SET created_by = 'system' WHERE created_by IS NULL;
ALTER TABLE facility ALTER COLUMN created_by SET NOT NULL;
ALTER TABLE facility ALTER COLUMN updated_by TYPE VARCHAR(100);

-- Alter audit columns in user_account table
ALTER TABLE user_account ALTER COLUMN created_by TYPE VARCHAR(100);
ALTER TABLE user_account ALTER COLUMN created_by SET DEFAULT 'system';
UPDATE user_account SET created_by = 'system' WHERE created_by IS NULL;
ALTER TABLE user_account ALTER COLUMN created_by SET NOT NULL;
ALTER TABLE user_account ALTER COLUMN updated_by TYPE VARCHAR(100);
