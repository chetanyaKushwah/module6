CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY,
    category JSONB,
    name VARCHAR(255) NOT NULL,
    photo_url TEXT NOT NULL,
    tags JSONB NOT NULL DEFAULT '[]'::jsonb,
    status VARCHAR(30)
);
