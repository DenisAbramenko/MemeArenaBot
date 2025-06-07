-- Create or replace a function to check and rename column
CREATE OR REPLACE FUNCTION rename_column_if_exists() RETURNS void AS
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'memes'
        AND column_name = 'incontest'
    ) THEN
        EXECUTE 'ALTER TABLE memes RENAME COLUMN incontest TO in_contest';
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Execute the function
SELECT rename_column_if_exists();

-- Drop the function
DROP FUNCTION rename_column_if_exists();

-- Set NOT NULL constraint for in_contest column
ALTER TABLE memes ALTER COLUMN in_contest SET NOT NULL;

-- Set default value for in_contest column
ALTER TABLE memes ALTER COLUMN in_contest SET DEFAULT false; 