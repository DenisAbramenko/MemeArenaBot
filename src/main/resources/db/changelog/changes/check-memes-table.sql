-- Проверка структуры таблицы memes
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'memes';

-- Проверка наличия NULL значений в поле in_contest
SELECT COUNT(*) AS null_count 
FROM memes 
WHERE in_contest IS NULL; 