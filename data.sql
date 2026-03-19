-- 1. Tabel User (Manajemen Profile & Auth)
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

-- 2. Tabel Refresh Tokens (Keamanan JWT)
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token TEXT NOT NULL,
    auth_token TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 3. Tabel Artists (Data Artis SM Entertainment)
CREATE TABLE IF NOT EXISTS artists (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(20) NOT NULL,      -- 'Boy Group', 'Girl Group', 'Soloist'
    description TEXT NOT NULL,
    image_url TEXT NULL,
    debut_year INTEGER NOT NULL DEFAULT 2000,
    status VARCHAR(15) NOT NULL DEFAULT 'Active',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 4. Tabel Album (Satu Artist memiliki banyak Album)
CREATE TABLE IF NOT EXISTS albums (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    artist_id UUID NOT NULL REFERENCES artists(id) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL,
    release_date VARCHAR(20),
    cover_url TEXT NULL,
    -- KOLOM BARU: Agar sinkron dengan Kotlin AlbumTable
    type VARCHAR(30) NOT NULL DEFAULT 'Full Album',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 5. Tabel Favorites (Bias System)
CREATE TABLE IF NOT EXISTS favorites (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    artist_id UUID NOT NULL REFERENCES artists(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, artist_id)
    );