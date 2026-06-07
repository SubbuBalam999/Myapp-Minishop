INSERT INTO users (name, email)
VALUES
    ('Ada Lovelace', 'ada@example.com'),
    ('Grace Hopper', 'grace@example.com')
ON CONFLICT (email) DO NOTHING;
