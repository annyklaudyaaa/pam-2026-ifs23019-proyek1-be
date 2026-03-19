package org.delcom.repositories

import org.delcom.entities.Artist

interface IFavoriteRepository {
    /**
     * Fitur Utama Bias System:
     * Jika belum favorit maka ditambah, jika sudah ada maka dihapus.
     * Return true jika ditambahkan, false jika dihapus.
     */
    suspend fun toggleFavorite(userId: String, artistId: String): Boolean

    /**
     * Mengambil daftar semua "Bias" (Artis Favorit) milik User tertentu.
     * Akan digunakan di Tab "My Bias" pada aplikasi Android nanti.
     */
    suspend fun getFavorites(userId: String): List<Artist>

    /**
     * Mengecek status apakah artis ini adalah favorit user.
     * Sangat penting untuk menentukan apakah ikon Hati (Heart) di UI Android
     * harus berwarna merah atau tidak.
     */
    suspend fun isFavorite(userId: String, artistId: String): Boolean
}