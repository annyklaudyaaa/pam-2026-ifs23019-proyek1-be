package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ArtistTable : UUIDTable("artists") { // Nama tabel di database menjadi 'artists'
    // Menghubungkan artis ke user/admin yang menginputnya
    val userId = uuid("user_id").references(UserTable.id)

    // Informasi Artis SM Entertainment
    val name = varchar("name", 255)           // Nama Artis (misal: "Mark Lee")
    val groupName = varchar("group_name", 255) // Nama Grup (misal: "NCT 127")
    val photoUrl = text("photo_url").nullable() // URL foto profil
    val position = varchar("position", 100).default("Member") // Posisi (Vocal, Dancer, dll)
    val isActive = bool("is_active").default(true) // Status aktif

    // Metadata
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}