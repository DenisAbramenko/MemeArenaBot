-- Add column if not exists
ALTER TABLE memes ADD COLUMN IF NOT EXISTS in_contest BOOLEAN;

-- Set default values for existing records
UPDATE memes SET in_contest = false WHERE in_contest IS NULL;

-- Set NOT NULL constraint
ALTER TABLE memes ALTER COLUMN in_contest SET NOT NULL;

-- Set default value for new records
ALTER TABLE memes ALTER COLUMN in_contest SET DEFAULT false; 