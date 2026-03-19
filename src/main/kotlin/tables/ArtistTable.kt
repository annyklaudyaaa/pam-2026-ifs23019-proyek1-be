package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ArtistTable : UUIDTable("artists") {
    // Menghubungkan ke UserTable (siapa yang menambah/mengelola data ini)
    val userId = uuid("user_id").references(UserTable.id)

    // Nama Artis atau Grup (Contoh: "aespa", "NCT 127", "Taeyeon")
    val name = varchar("name", 100)

    // Kategori untuk fitur FILTER (Contoh: "Boy Group", "Girl Group", "Soloist")
    val category = varchar("category", 20)

    // Deskripsi atau Bio singkat
    val description = text("description")

    // URL Foto untuk cover/profile (Gunakan text karena URL bisa panjang)
    val imageUrl = text("image_url").nullable()

    // Tahun Debut (Bisa untuk tambahan filter/sorting)
    val debutYear = integer("debut_year").default(2000)

    // Status (Misal: Active / Inactive)
    val status = varchar("status", 15).default("Active")

    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}