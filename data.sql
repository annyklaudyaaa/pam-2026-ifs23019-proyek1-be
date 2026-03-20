-- 1. Tabel Users (Tetap sama untuk manajemen akun admin/user)
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    photo VARCHAR(255) NULL,
    about TEXT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

-- 2. Tabel Refresh Tokens (Tetap sama untuk kebutuhan autentikasi JWT)
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token TEXT NOT NULL,
    auth_token TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
    );

-- 3. Tabel Artists (Ganti dari 'todos' untuk topik SM Entertainment)
CREATE TABLE IF NOT EXISTS artists (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,              -- Nama Artis (ex: Karina, Mark Lee)
    group_name VARCHAR(100) NOT NULL,        -- Nama Grup (ex: aespa, NCT)
    position VARCHAR(100) NOT NULL DEFAULT 'Member', -- Posisi dalam grup
    is_active BOOLEAN NOT NULL DEFAULT TRUE, -- Status aktif di agensi
    photo_url TEXT NULL,                     -- Path/URL foto artis
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );