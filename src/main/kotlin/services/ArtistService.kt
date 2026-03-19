package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.ArtistRequest
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.*
import java.io.File
import java.util.*

class ArtistService(
    private val userRepo: IUserRepository,
    private val artistRepo: IArtistRepository,
    private val albumRepo: IAlbumRepository,
    private val favoriteRepo: IFavoriteRepository
) {
    // 1. Ambil Semua (Search & Filter)
    suspend fun getAll(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val search = call.request.queryParameters["search"] ?: ""
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val perPage = call.request.queryParameters["perPage"]?.toIntOrNull() ?: 10
        val category = call.request.queryParameters["category"]
        val status = call.request.queryParameters["status"]

        val artists = artistRepo.getAll(user.id, search, page, perPage, category, status)
        call.respond(DataResponse("success", "Berhasil", mapOf("artists" to artists)))
    }

    // 2. Detail Artis + Diskografi (Untuk Interactive Discography & Shared Element)
    suspend fun getById(call: ApplicationCall) {
        val artistId = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val data = artistRepo.getByIdWithAlbums(artistId)
        if (data == null || data.artist.userId != user.id) {
            throw AppException(404, "Artis tidak ditemukan")
        }

        // Cek status favorit untuk ikon hati di Android
        val isFav = favoriteRepo.isFavorite(user.id, artistId)

        call.respond(DataResponse("success", "Detail Artis", mapOf(
            "artist" to data.artist,
            "albums" to data.albums,
            "isFavorite" to isFav
        )))
    }

    // 3. Bias System: Toggle Favorite
    suspend fun toggleFavorite(call: ApplicationCall) {
        val artistId = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val isAdded = favoriteRepo.toggleFavorite(user.id, artistId)
        val msg = if (isAdded) "Ditambahkan ke Bias" else "Dihapus dari Bias"
        call.respond(DataResponse("success", msg, mapOf("isFavorite" to isAdded)))
    }

    // 4. Statistik Dashboard
    suspend fun getStats(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val stats = artistRepo.getStats(user.id)
        call.respond(DataResponse("success", "Statistik", mapOf("stats" to stats)))
    }

    // 5. Create Artist
    suspend fun post(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<ArtistRequest>()
        request.userId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("name", "Nama wajib diisi").validate()

        val id = artistRepo.create(request.toEntity())
        call.respond(DataResponse("success", "Berhasil", mapOf("artistId" to id)))
    }

    // 6. Upload Gambar Artis
    suspend fun putImage(call: ApplicationCall) {
        val artistId = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val multipart = call.receiveMultipart()
        var filePath: String? = null

        multipart.forEachPart { part ->
            if (part is PartData.FileItem) {
                val fileName = "${UUID.randomUUID()}.${part.originalFileName?.substringAfterLast('.')}"
                filePath = "uploads/artists/$fileName"
                val file = File(filePath!!)
                file.parentFile.mkdirs()
                part.provider().copyAndClose(file.writeChannel())
            }
            part.dispose()
        }

        val oldArtist = artistRepo.getById(artistId)
        if (oldArtist == null || oldArtist.userId != user.id) throw AppException(404, "Not Found")

        val newEntity = oldArtist.copy(imageUrl = filePath, updatedAt = kotlinx.datetime.Clock.System.now())
        artistRepo.update(user.id, artistId, newEntity)

        call.respond(DataResponse("success", "Gambar diperbarui", null))
    }

    // 7. Stream Image ke Android
    suspend fun getImage(call: ApplicationCall) {
        val artistId = call.parameters["id"] ?: return call.respond(HttpStatusCode.BadRequest)
        val artist = artistRepo.getById(artistId) ?: return call.respond(HttpStatusCode.NotFound)
        val file = File(artist.imageUrl ?: "")
        if (file.exists()) call.respondFile(file) else call.respond(HttpStatusCode.NotFound)
    }
}