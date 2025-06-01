-- Update existing rows in memes table with default values
UPDATE memes SET created_at = NOW() WHERE created_at IS NULL;
UPDATE memes SET in_contest = false WHERE in_contest IS NULL;
UPDATE memes SET published_to_feed = false WHERE published_to_feed IS NULL;
UPDATE memes SET type = 'TEMPLATE_BASED' WHERE type IS NULL;
UPDATE memes SET user_id = '0' WHERE user_id IS NULL;

-- Update existing rows in users table with default values
UPDATE users SET created_at = NOW() WHERE created_at IS NULL;
UPDATE users SET is_admin = false WHERE is_admin IS NULL;
UPDATE users SET is_premium = false WHERE is_premium IS NULL;
UPDATE users SET last_activity = NOW() WHERE last_activity IS NULL;
UPDATE users SET total_likes = 0 WHERE total_likes IS NULL;
UPDATE users SET total_memes = 0 WHERE total_memes IS NULL; 