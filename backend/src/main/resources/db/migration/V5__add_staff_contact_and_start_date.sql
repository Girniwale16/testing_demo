-- Add contact and start_date columns required by the staff create/update feature.
ALTER TABLE staff ADD COLUMN contact VARCHAR(50);
ALTER TABLE staff ADD COLUMN start_date DATE;
