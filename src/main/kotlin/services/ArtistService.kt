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
import org.delcom.repositories.IArtistRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.*

class ArtistService(
    private val userRepo: IUserRepository,
    private val artistRepo: IArtistRepository
) {
    // 1. Mengambil semua daftar artis dengan dukungan Pagination, Search (Nama), dan Filter (Grup)
    suspend fun getAll(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        // Ambil Query Parameters dari URL
        val search = call.request.queryParameters["search"] ?: ""
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val perPage = call.request.queryParameters["perPage"]?.toIntOrNull() ?: 10
        val isActive = call.request.queryParameters["is_active"]?.toBooleanStrictOrNull()
        val groupName = call.request.queryParameters["group_name"] // Filter berdasarkan Grup (misal: aespa)

        // Panggil Repo dengan parameter bertema SM Entertainment
        val artists = artistRepo.getAll(user.id, search, page, perPage, isActive, groupName)

        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar artis",
            mapOf(Pair("artists", artists))
        )
        call.respond(response)
    }

    // 2. Mengambil statistik untuk Dashboard
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

    // 3. Mengambil data artis berdasarkan ID
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

    // 4. Menambahkan artis baru
    suspend fun post(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<ArtistRequest>()
        request.userId = user.id

        // Validasi input untuk SM Entertainment
        val validator = ValidatorHelper(request.toMap())
        validator.required("name", "Nama artis tidak boleh kosong")
        validator.required("groupName", "Nama grup tidak boleh kosong")
        validator.required("position", "Posisi/Role tidak boleh kosong")
        validator.validate()

        val artistId = artistRepo.create(request.toEntity())

        val response = DataResponse(
            "success",
            "Berhasil menambahkan data artis",
            mapOf(Pair("artistId", artistId))
        )
        call.respond(response)
    }

    // 5. Mengubah data teks artis
    suspend fun put(call: ApplicationCall) {
        val artistId = call.parameters["id"]
            ?: throw AppException(400, "ID Artis tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<ArtistRequest>()
        request.userId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("name", "Nama artis tidak boleh kosong")
        validator.required("groupName", "Nama grup tidak boleh kosong")
        validator.required("position", "Posisi tidak boleh kosong")
        validator.required("isActive", "Status aktif tidak boleh kosong")
        validator.validate()

        val oldArtist = artistRepo.getById(artistId)
        if (oldArtist == null || oldArtist.userId != user.id) {
            throw AppException(404, "Data artis tidak tersedia!")
        }

        // Mempertahankan foto lama saat update data teks
        request.photoUrl = oldArtist.photoUrl

        val isUpdated = artistRepo.update(user.id, artistId, request.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data artis!")
        }

        val response = DataResponse("success", "Berhasil mengubah data artis", null)
        call.respond(response)
    }

    // 6. Mengubah foto artis (Upload File)
    suspend fun putPhoto(call: ApplicationCall) {
        val artistId = call.parameters["id"]
            ?: throw AppException(400, "ID Artis tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = ArtistRequest()
        request.userId = user.id

        // Proses upload file foto ke folder uploads/artists/
        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            if (part is PartData.FileItem) {
                val ext = part.originalFileName
                    ?.substringAfterLast('.', "")
                    ?.let { if (it.isNotEmpty()) ".$it" else "" }
                    ?: ""

                val fileName = UUID.randomUUID().toString() + ext
                val filePath = "uploads/artists/$fileName"

                withContext(Dispatchers.IO) {
                    val file = File(filePath)
                    file.parentFile.mkdirs()
                    part.provider().copyAndClose(file.writeChannel())
                    request.photoUrl = filePath
                }
            }
            part.dispose()
        }

        if (request.photoUrl == null) {
            throw AppException(404, "File foto tidak ditemukan!")
        }

        val oldArtist = artistRepo.getById(artistId)
        if (oldArtist == null || oldArtist.userId != user.id) {
            throw AppException(404, "Data artis tidak tersedia!")
        }

        // Sinkronisasi data lama agar tidak hilang saat update foto
        request.name = oldArtist.name
        request.groupName = oldArtist.groupName
        request.position = oldArtist.position
        request.isActive = oldArtist.isActive

        val isUpdated = artistRepo.update(user.id, artistId, request.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui foto artis!")
        }

        // Hapus file foto lama jika ada
        oldArtist.photoUrl?.let { path ->
            val oldFile = File(path)
            if (oldFile.exists()) oldFile.delete()
        }

        call.respond(DataResponse("success", "Berhasil mengubah foto artis", null))
    }

    // 7. Menghapus data artis
    suspend fun delete(call: ApplicationCall) {
        val artistId = call.parameters["id"] ?: throw AppException(400, "ID Artis tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val oldArtist = artistRepo.getById(artistId)

        if (oldArtist == null || oldArtist.userId != user.id) {
            throw AppException(404, "Data artis tidak tersedia!")
        }

        val isDeleted = artistRepo.delete(user.id, artistId)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus data artis!")
        }

        // Hapus file fisik foto dari server
        oldArtist.photoUrl?.let { path ->
            val file = File(path)
            if (file.exists()) file.delete()
        }

        call.respond(DataResponse("success", "Berhasil menghapus data artis", null))
    }

    // 8. Mengambil file gambar untuk UI Android
    suspend fun getPhoto(call: ApplicationCall) {
        val artistId = call.parameters["id"] ?: throw AppException(400, "ID Artis tidak valid!")
        val artist = artistRepo.getById(artistId) ?: return call.respond(HttpStatusCode.NotFound)

        val filePath = artist.photoUrl ?: throw AppException(404, "Artis belum memiliki foto profil")
        val file = File(filePath)

        if (!file.exists()) throw AppException(404, "File foto tidak ditemukan di server")

        call.respondFile(file)
    }
}