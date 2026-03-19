-- 1. Tabel User (Tetap untuk Manajemen Profile & Auth)
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    photo VARCHAR(255) NULL,
    about TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 2. Tabel Refresh Tokens (Tetap untuk Keamanan JWT)
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token TEXT NOT NULL,
    auth_token TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 3. Tabel Artists (Refactor dari 'todos' untuk SM Entertainment)
CREATE TABLE IF NOT EXISTS artists (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,         -- Contoh: "NCT 127", "Red Velvet"
    category VARCHAR(20) NOT NULL,      -- Contoh: "Boy Group", "Girl Group", "Soloist"
    description TEXT NOT NULL,          -- Bio/Diskografi singkat
    image_url TEXT NULL,                -- Foto Artis
    debut_year INTEGER NOT NULL DEFAULT 2000,
    status VARCHAR(15) NOT NULL DEFAULT 'Active', -- Contoh: "Active", "Hiatus"
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Tabel Album (1 Artist bisa punya banyak Album)
CREATE TABLE IF NOT EXISTS albums (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    artist_id UUID NOT NULL REFERENCES artists(id) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL,
    release_date VARCHAR(20),
    cover_url TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Tabel Favorites (Relasi antara User dan Artist favoritnya)
CREATE TABLE IF NOT EXISTS favorites (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    artist_id UUID NOT NULL REFERENCES artists(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, artist_id) -- Supaya user tidak fav artis yang sama dua kali
    );