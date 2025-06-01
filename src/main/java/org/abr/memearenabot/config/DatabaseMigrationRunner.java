package org.abr.memearenabot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseMigrationRunner {
    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrationRunner.class);

    @Bean
    public CommandLineRunner migrateDatabaseSchema(JdbcTemplate jdbcTemplate) {
        return args -> {
            log.info("Running database schema migration...");
            
            try {
                // Add missing columns with default values to memes table
                jdbcTemplate.execute("ALTER TABLE memes ADD COLUMN IF NOT EXISTS created_at TIMESTAMP");
                jdbcTemplate.execute("UPDATE memes SET created_at = NOW() WHERE created_at IS NULL");
                jdbcTemplate.execute("ALTER TABLE memes ALTER COLUMN created_at SET NOT NULL");
                
                jdbcTemplate.execute("ALTER TABLE memes ADD COLUMN IF NOT EXISTS in_contest BOOLEAN");
                jdbcTemplate.execute("UPDATE memes SET in_contest = false WHERE in_contest IS NULL");
                jdbcTemplate.execute("ALTER TABLE memes ALTER COLUMN in_contest SET NOT NULL");
                
                jdbcTemplate.execute("ALTER TABLE memes ADD COLUMN IF NOT EXISTS published_to_feed BOOLEAN");
                jdbcTemplate.execute("UPDATE memes SET published_to_feed = false WHERE published_to_feed IS NULL");
                jdbcTemplate.execute("ALTER TABLE memes ALTER COLUMN published_to_feed SET NOT NULL");
                
                jdbcTemplate.execute("ALTER TABLE memes ADD COLUMN IF NOT EXISTS type VARCHAR(255)");
                jdbcTemplate.execute("UPDATE memes SET type = 'TEMPLATE_BASED' WHERE type IS NULL");
                jdbcTemplate.execute("ALTER TABLE memes ALTER COLUMN type SET NOT NULL");
                
                // Check if constraint exists before adding it
                Integer constraintCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM pg_constraint WHERE conname = 'meme_type_check'", Integer.class);
                if (constraintCount != null && constraintCount == 0) {
                    jdbcTemplate.execute("ALTER TABLE memes ADD CONSTRAINT meme_type_check CHECK (type IN ('AI_GENERATED','TEMPLATE_BASED','VOICE_GENERATED'))");
                    log.info("Added meme_type_check constraint");
                } else {
                    log.info("meme_type_check constraint already exists, skipping");
                }
                
                jdbcTemplate.execute("ALTER TABLE memes ADD COLUMN IF NOT EXISTS user_id VARCHAR(255)");
                jdbcTemplate.execute("UPDATE memes SET user_id = '0' WHERE user_id IS NULL");
                jdbcTemplate.execute("ALTER TABLE memes ALTER COLUMN user_id SET NOT NULL");
                
                // Add missing columns with default values to users table
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at TIMESTAMP");
                jdbcTemplate.execute("UPDATE users SET created_at = NOW() WHERE created_at IS NULL");
                jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN created_at SET NOT NULL");
                
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS is_admin BOOLEAN");
                jdbcTemplate.execute("UPDATE users SET is_admin = false WHERE is_admin IS NULL");
                jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN is_admin SET NOT NULL");
                
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS is_premium BOOLEAN");
                jdbcTemplate.execute("UPDATE users SET is_premium = false WHERE is_premium IS NULL");
                jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN is_premium SET NOT NULL");
                
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS last_activity TIMESTAMP");
                jdbcTemplate.execute("UPDATE users SET last_activity = NOW() WHERE last_activity IS NULL");
                jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN last_activity SET NOT NULL");
                
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS total_likes INTEGER");
                jdbcTemplate.execute("UPDATE users SET total_likes = 0 WHERE total_likes IS NULL");
                jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN total_likes SET NOT NULL");
                
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS total_memes INTEGER");
                jdbcTemplate.execute("UPDATE users SET total_memes = 0 WHERE total_memes IS NULL");
                jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN total_memes SET NOT NULL");
                
                // Create indexes
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_meme_user_id ON memes(user_id)");
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_meme_in_contest ON memes(in_contest)");
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_meme_created_at ON memes(created_at)");
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_created_at ON users(created_at)");
                
                // Create missing contest_entries table if it doesn't exist
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS contest_entries (" +
                        "id BIGSERIAL PRIMARY KEY," +
                        "contest_id BIGINT NOT NULL," +
                        "meme_id BIGINT NOT NULL," +
                        "created_at TIMESTAMP NOT NULL DEFAULT NOW()," +
                        "votes INTEGER NOT NULL DEFAULT 0" +
                        ")");
                
                // Create indexes for contest_entries table
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_contest_entries_contest_id ON contest_entries(contest_id)");
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_contest_entries_meme_id ON contest_entries(meme_id)");
                
                // Create missing contests table if it doesn't exist
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS contests (" +
                        "id BIGSERIAL PRIMARY KEY," +
                        "title VARCHAR(255) NOT NULL," +
                        "description VARCHAR(2000)," +
                        "created_by BIGINT NOT NULL," +
                        "created_at TIMESTAMP NOT NULL DEFAULT NOW()," +
                        "start_date TIMESTAMP NOT NULL," +
                        "end_date TIMESTAMP NOT NULL," +
                        "status VARCHAR(20) NOT NULL," +
                        "prize_description VARCHAR(1000)," +
                        "max_entries_per_user INTEGER NOT NULL DEFAULT 3" +
                        ")");
                
                // Create indexes for contests table
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_contest_status ON contests(status)");
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_contest_dates ON contests(start_date, end_date)");
                
                // Create missing contest_votes table if it doesn't exist
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS contest_votes (" +
                        "id BIGSERIAL PRIMARY KEY," +
                        "entry_id BIGINT NOT NULL," +
                        "user_id BIGINT NOT NULL," +
                        "voted_at TIMESTAMP NOT NULL DEFAULT NOW()," +
                        "CONSTRAINT uk_user_entry_vote UNIQUE (user_id, entry_id)" +
                        ")");
                
                // Create indexes for contest_votes table
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_vote_entry ON contest_votes(entry_id)");
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_vote_user ON contest_votes(user_id)");
                
                log.info("Database schema migration completed successfully");
            } catch (Exception e) {
                log.error("Error during database migration", e);
                throw e;
            }
        };
    }
} 