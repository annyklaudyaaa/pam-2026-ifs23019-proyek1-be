package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object AlbumTable : UUIDTable("albums") {
    // Relasi: Menghubungkan album ke Artis tertentu
    // onDelete = ReferenceOption.CASCADE memastikan jika Artis dihapus, semua albumnya juga ikut terhapus otomatis
    val artistId = uuid("artist_id").references(ArtistTable.id, onDelete = ReferenceOption.CASCADE)

    // Judul Album (Contoh: "Whiplash", "Armageddon", "Savage")
    val title = varchar("title", 100)

    // Tanggal Rilis (Bisa String agar fleksibel atau Timestamp)
    val releaseDate = varchar("release_date", 20)

    // URL/Path untuk cover album (Krusial untuk visual di Android nanti)
    val coverUrl = text("cover_url").nullable()

    // Tipe Album (Contoh: "Full Album", "Mini Album", "Digital Single")
    val type = varchar("type", 30).default("Full Album")

    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}