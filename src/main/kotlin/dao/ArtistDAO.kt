package org.delcom.dao

import org.delcom.tables.ArtistTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class ArtistDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, ArtistDAO>(ArtistTable)

    // Menyimpan ID user/admin yang menginput data ini
    var userId by ArtistTable.userId

    // Informasi Utama Artis
    var name by ArtistTable.name           // Nama Artis (misal: "Karina")
    var groupName by ArtistTable.groupName // Nama Grup (misal: "aespa")
    var photoUrl by ArtistTable.photoUrl   // URL foto artis
    var position by ArtistTable.position   // Posisi (misal: "Main Dancer")
    var isActive by ArtistTable.isActive   // Status aktif di industri

    // Metadata
    var createdAt by ArtistTable.createdAt
    var updatedAt by ArtistTable.updatedAt
}