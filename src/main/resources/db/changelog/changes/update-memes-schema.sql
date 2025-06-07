-- Проверка существования колонки in_contest
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'memes'
        AND column_name = 'incontest'
    ) THEN
        -- Переименование колонки incontest в in_contest
        ALTER TABLE memes RENAME COLUMN incontest TO in_contest;
    END IF;
END $$;

-- Установка NOT NULL ограничения для колонки in_contest
ALTER TABLE memes ALTER COLUMN in_contest SET NOT NULL;

-- Установка значения по умолчанию для колонки in_contest
ALTER TABLE memes ALTER COLUMN in_contest SET DEFAULT false; 