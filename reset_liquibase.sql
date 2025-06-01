-- This script resets Liquibase's state in the database
-- It will allow Liquibase to rerun migrations with the new preConditions

-- Drop Liquibase tables
DROP TABLE IF EXISTS DATABASECHANGELOGLOCK;
DROP TABLE IF EXISTS DATABASECHANGELOG;

-- Note: Run this script directly against your database before restarting the application
-- For example:
-- psql -U postgres -d memarenadb -f reset_liquibase.sql 