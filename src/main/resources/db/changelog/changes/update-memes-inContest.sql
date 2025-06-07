-- Update all records in memes table where in_contest is NULL
UPDATE memes SET in_contest = false WHERE in_contest IS NULL; 