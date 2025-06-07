package org.abr.memearenabot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Database initializer that runs before Hibernate validation.
 * This ensures tables and columns exist before Hibernate attempts to validate them.
 */
@Configuration
@Order(1)  // High priority to run before other beans
public class DatabaseInitializer implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Initializing database schema before Hibernate validation...");

        try {
            // Add missing columns with default values to memes table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS memes (" + "id BIGSERIAL PRIMARY KEY," + "url VARCHAR" +
                    "(255) NOT NULL," + "likes INTEGER NOT NULL DEFAULT 0," + "created_at TIMESTAMP NOT NULL DEFAULT " +
                    "NOW()," + "in_contest BOOLEAN NOT NULL DEFAULT false," + "published_to_feed BOOLEAN NOT NULL " +
                    "DEFAULT false," + "type VARCHAR(255) NOT NULL DEFAULT 'TEMPLATE_BASED'," + "user_id VARCHAR(255)" +
                    " NOT NULL DEFAULT '0'" + ")");

            // Create indexes for memes table
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_meme_user_id ON memes(user_id)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_meme_in_contest ON memes(in_contest)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_meme_created_at ON memes(created_at)");

            // Create users table if it doesn't exist
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (" + "id BIGSERIAL PRIMARY KEY," + "telegram_id " +
                    "VARCHAR(255) NOT NULL UNIQUE," + "username VARCHAR(255)," + "first_name VARCHAR(255)," +
                    "last_name VARCHAR(255)," + "language_code VARCHAR(10)," + "created_at TIMESTAMP NOT NULL DEFAULT" +
                    " NOW()," + "last_activity TIMESTAMP NOT NULL DEFAULT NOW()," + "is_admin BOOLEAN NOT NULL " +
                    "DEFAULT false," + "is_premium BOOLEAN NOT NULL DEFAULT false," + "total_memes INTEGER NOT NULL " +
                    "DEFAULT 0," + "total_likes INTEGER NOT NULL DEFAULT 0" + ")");

            // Create indexes for users table
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_created_at ON users(created_at)");

            // Create missing contest_entries table if it doesn't exist
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS contest_entries (" + "id BIGSERIAL PRIMARY KEY," +
                    "contest_id BIGINT NOT NULL," + "meme_id BIGINT NOT NULL," + "created_at TIMESTAMP NOT NULL " +
                    "DEFAULT NOW()," + "votes INTEGER NOT NULL DEFAULT 0" + ")");

            // Create indexes for contest_entries table
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_contest_entries_contest_id ON contest_entries" +
                    "(contest_id)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_contest_entries_meme_id ON contest_entries(meme_id)");

            // Create missing contests table if it doesn't exist
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS contests (" + "id BIGSERIAL PRIMARY KEY," + "title " +
                    "VARCHAR(255) NOT NULL," + "description VARCHAR(2000)," + "created_by BIGINT NOT NULL," +
                    "created_at TIMESTAMP NOT NULL DEFAULT NOW()," + "start_date TIMESTAMP NOT NULL," + "end_date " +
                    "TIMESTAMP NOT NULL," + "status VARCHAR(20) NOT NULL," + "prize_description VARCHAR(1000)," +
                    "max_entries_per_user INTEGER NOT NULL DEFAULT 3" + ")");

            // Create indexes for contests table
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_contest_status ON contests(status)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_contest_dates ON contests(start_date, end_date)");

            // Create missing contest_votes table if it doesn't exist
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS contest_votes (" + "id BIGSERIAL PRIMARY KEY," +
                    "entry_id BIGINT NOT NULL," + "user_id BIGINT NOT NULL," + "voted_at TIMESTAMP NOT NULL DEFAULT " +
                    "NOW()," + "CONSTRAINT uk_user_entry_vote UNIQUE (user_id, entry_id)" + ")");

            // Create indexes for contest_votes table
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_vote_entry ON contest_votes(entry_id)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_vote_user ON contest_votes(user_id)");

            log.info("Database schema initialization completed successfully");
        } catch (Exception e) {
            log.error("Error during database initialization", e);
            throw e;
        }
    }
} 