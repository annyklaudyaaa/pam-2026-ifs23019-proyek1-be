package org.delcom.services

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.delcom.data.AppException
import org.delcom.data.AuthRequest
import org.delcom.data.DataResponse
import org.delcom.data.UserResponse
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.helpers.hashPassword
import org.delcom.helpers.verifyPassword
import org.delcom.repositories.IRefreshTokenRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.*

class UserService(
    private val userRepo: IUserRepository,
    private val refreshTokenRepo: IRefreshTokenRepository,
) {
    // 1. Ambil data profil saya
    suspend fun getMe(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val response = DataResponse(
            "success",
            "Berhasil mengambil informasi akun saya",
            mapOf(
                "user" to UserResponse(
                    id = user.id,
                    name = user.name,
                    username = user.username,
                    photo = user.photo,
                    about = user.about,
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt,
                ),
            )
        )
        call.respond(response)
    }

    // 2. Update profil (Nama, Username, About)
    suspend fun putMe(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<AuthRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("name", "Nama tidak boleh kosong")
        validator.required("username", "Username tidak boleh kosong")
        validator.validate()

        val existUser = userRepo.getByUsername(request.username)
        if (existUser != null && existUser.username != user.username) {
            // FIX: Hapus errors.joinToString karena ini logika database, bukan validasi input
            throw AppException(409, "Akun dengan username ini sudah terdaftar!")
        }

        user.username = request.username
        user.name = request.name
        user.about = request.about

        val isUpdated = userRepo.update(user.id, user)
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data profile!")
        }

        call.respond(DataResponse("success", "Berhasil mengubah data profile", null))
    }

    // 3. Update Photo Profile
    suspend fun putMyPhoto(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        var newPhoto: String? = null
        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            if (part is PartData.FileItem) {
                val ext = part.originalFileName?.substringAfterLast('.', "")
                    ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""

                val fileName = UUID.randomUUID().toString() + ext
                val filePath = "uploads/users/$fileName"

                withContext(Dispatchers.IO) {
                    val file = File(filePath)
                    file.parentFile.mkdirs()
                    part.provider().copyAndClose(file.writeChannel())
                    newPhoto = filePath
                }
            }
            part.dispose()
        }

        if (newPhoto == null) {
            throw AppException(400, "Photo profile tidak tersedia!")
        }

        val oldPhoto = user.photo
        user.photo = newPhoto

        val isUpdated = userRepo.update(user.id, user)
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui database foto!")
        }

        // Hapus file lama agar server tidak penuh sampah
        if (oldPhoto != null) {
            File(oldPhoto).apply { if (exists()) delete() }
        }

        call.respond(DataResponse("success", "Berhasil mengubah photo profile", null))
    }

    // 4. Ganti Password
    suspend fun putMyPassword(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<AuthRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("newPassword", "Kata sandi baru wajib diisi")
        validator.required("password", "Kata sandi lama wajib diisi")
        validator.validate()

        val validPassword = verifyPassword(request.password, user.password)
        if (!validPassword) {
            throw AppException(401, "Kata sandi lama tidak valid!")
        }

        user.password = hashPassword(request.newPassword)
        val isUpdated = userRepo.update(user.id, user)
        if (!isUpdated) {
            throw AppException(400, "Gagal mengubah kata sandi!")
        }

        refreshTokenRepo.deleteByUserId(user.id)
        call.respond(DataResponse("success", "Password berhasil diubah", null))
    }

    // 5. Stream Photo ke UI
    suspend fun getPhoto(call: ApplicationCall) {
        val userId = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val user = userRepo.getById(userId) ?: throw AppException(404, "User tidak ditemukan")

        val photoPath = user.photo ?: throw AppException(404, "User belum memiliki foto")
        val file = File(photoPath)

        if (!file.exists()) throw AppException(404, "File foto tidak ditemukan di server")

        call.respondFile(file)
    }
}