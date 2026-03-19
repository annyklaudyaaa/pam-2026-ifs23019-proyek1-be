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
import org.delcom.data.ArtistRequest // Pastikan sudah membuat ArtistRequest
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IArtistRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.*

class ArtistService(
    private val userRepo: IUserRepository,
    private val artistRepo: IArtistRepository
) {
    // Mengambil semua daftar artis dengan dukungan Pagination, Search, dan Filter Kategori
    suspend fun getAll(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        // Query Parameters untuk Fitur Search & Filter sesuai spek PAM
        val search = call.request.queryParameters["search"] ?: ""
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val perPage = call.request.queryParameters["perPage"]?.toIntOrNull() ?: 10

        // Filter spesifik topik SM Entertainment
        val category = call.request.queryParameters["category"] // Boy Group, Girl Group, Soloist
        val status = call.request.queryParameters["status"] // Active, Inactive

        val artists = artistRepo.getAll(user.id, search, page, perPage, category, status)

        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar artis SM Entertainment",
            mapOf(Pair("artists", artists))
        )
        call.respond(response)
    }

    // Mengambil statistik (Misal: Jumlah artis per kategori) untuk Dashboard Home
    suspend fun getStats(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val stats = artistRepo.getStats(user.id)

        val response = DataResponse(
            "success",
            "Berhasil mengambil statistik artis",
            mapOf(Pair("stats", stats))
        )
        call.respond(response)
    }

    // Mengambil detail artis berdasarkan ID
    suspend fun getById(call: ApplicationCall) {
        val artistId = call.parameters["id"]
            ?: throw AppException(400, "ID Artis tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val artist = artistRepo.getById(artistId)
        if (artist == null || artist.userId != user.id) {
            throw AppException(404, "Data artis tidak ditemukan!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengambil data artis",
            mapOf(Pair("artist", artist))
        )
        call.respond(response)
    }

    // Menambahkan artis baru
    suspend fun post(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<ArtistRequest>()
        request.userId = user.id

        // Validasi input sesuai field Artist
        val validator = ValidatorHelper(request.toMap())
        validator.required("name", "Nama artis tidak boleh kosong")
        validator.required("category", "Kategori (Boy/Girl Group/Soloist) wajib diisi")
        validator.required("description", "Deskripsi tidak boleh kosong")
        validator.validate()

        val artistId = artistRepo.create(request.toEntity())

        val response = DataResponse(
            "success",
            "Berhasil menambahkan artis baru",
            mapOf(Pair("artistId", artistId))
        )
        call.respond(response)
    }

    // Mengupdate data teks artis
    suspend fun put(call: ApplicationCall) {
        val artistId = call.parameters["id"]
            ?: throw AppException(400, "ID Artis tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<ArtistRequest>()
        request.userId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("name", "Nama tidak boleh kosong")
        validator.required("category", "Kategori tidak boleh kosong")
        validator.validate()

        val oldArtist = artistRepo.getById(artistId)
        if (oldArtist == null || oldArtist.userId != user.id) {
            throw AppException(404, "Data artis tidak tersedia!")
        }

        // Mempertahankan foto lama saat update data teks
        request.imageUrl = oldArtist.imageUrl

        val isUpdated = artistRepo.update(user.id, artistId, request.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data artis!")
        }

        call.respond(DataResponse("success", "Berhasil mengubah data artis", null))
    }

    // Upload/Ganti Foto Artis (imageUrl)
    suspend fun putImage(call: ApplicationCall) {
        val artistId = call.parameters["id"]
            ?: throw AppException(400, "ID Artis tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)
        val request = ArtistRequest()
        request.userId = user.id

        // Proses upload file
        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            if (part is PartData.FileItem) {
                val ext = part.originalFileName?.substringAfterLast('.', "")
                    ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""

                val fileName = UUID.randomUUID().toString() + ext
                val filePath = "uploads/artists/$fileName"

                withContext(Dispatchers.IO) {
                    val file = File(filePath)
                    file.parentFile.mkdirs()
                    part.provider().copyAndClose(file.writeChannel())
                    request.imageUrl = filePath
                }
            }
            part.dispose()
        }

        if (request.imageUrl == null) {
            throw AppException(400, "File gambar tidak ditemukan!")
        }

        val oldArtist = artistRepo.getById(artistId)
        if (oldArtist == null || oldArtist.userId != user.id) {
            throw AppException(404, "Data artis tidak tersedia!")
        }

        // Sinkronisasi data lama agar tidak null
        request.name = oldArtist.name
        request.category = oldArtist.category
        request.description = oldArtist.description
        request.status = oldArtist.status
        request.debutYear = oldArtist.debutYear

        val isUpdated = artistRepo.update(user.id, artistId, request.toEntity())

        // Hapus file lama jika update berhasil
        if (isUpdated) {
            oldArtist.imageUrl?.let { path ->
                val oldFile = File(path)
                if (oldFile.exists()) oldFile.delete()
            }
        }

        call.respond(DataResponse("success", "Berhasil mengubah foto artis", null))
    }

    // Menghapus data artis beserta filenya
    suspend fun delete(call: ApplicationCall) {
        val artistId = call.parameters["id"] ?: throw AppException(400, "ID tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val oldArtist = artistRepo.getById(artistId)

        if (oldArtist == null || oldArtist.userId != user.id) {
            throw AppException(404, "Data tidak ditemukan!")
        }

        if (artistRepo.delete(user.id, artistId)) {
            oldArtist.imageUrl?.let { path -> File(path).apply { if (exists()) delete() } }
            call.respond(DataResponse("success", "Berhasil menghapus artis", null))
        } else {
            throw AppException(400, "Gagal menghapus data")
        }
    }

    // Melayani file gambar untuk UI Android
    suspend fun getImage(call: ApplicationCall) {
        val artistId = call.parameters["id"] ?: throw AppException(400, "ID tidak valid!")
        val artist = artistRepo.getById(artistId) ?: return call.respond(HttpStatusCode.NotFound)

        val filePath = artist.imageUrl ?: throw AppException(404, "Artis belum memiliki foto")
        val file = File(filePath)

        if (!file.exists()) throw AppException(404, "File tidak tersedia di server")
        call.respondFile(file)
    }
}