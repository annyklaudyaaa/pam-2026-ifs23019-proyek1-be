package org.delcom

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.data.AppException
import org.delcom.data.ErrorResponse
import org.delcom.helpers.JWTConstants
import org.delcom.helpers.parseMessageToMap
import org.delcom.services.ArtistService
import org.delcom.services.AuthService
import org.delcom.services.UserService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    // Inject Service yang sudah didaftarkan di appModule
    val artistService: ArtistService by inject()
    val authService: AuthService by inject()
    val userService: UserService by inject()

    // Global Exception Handler (StatusPages)
    install(StatusPages) {
        exception<AppException> { call, cause ->
            val dataMap: Map<String, List<String>> = parseMessageToMap(cause.message)
            call.respond(
                status = HttpStatusCode.fromValue(cause.code),
                message = ErrorResponse(
                    status = "fail",
                    message = if (dataMap.isEmpty()) cause.message else "Data tidak valid!",
                    data = if (dataMap.isEmpty()) null else dataMap.toString()
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    status = "error",
                    message = cause.message ?: "Terjadi kesalahan pada server",
                    data = ""
                )
            )
        }
    }

    routing {
        // Endpoint Cek Status API
        get("/") {
            call.respondText("SM Entertainment API is running. Welcome, Anny!")
        }

        // --- AUTHENTICATION ROUTES ---
        route("/auth") {
            post("/login") { authService.postLogin(call) }
            post("/register") { authService.postRegister(call) }
            post("/refresh-token") { authService.postRefreshToken(call) }
            post("/logout") { authService.postLogout(call) }
        }

        // --- PROTECTED ROUTES (Requires JWT) ---
        authenticate(JWTConstants.NAME) {

            // Manajemen User & Profile (Fitur Edit Profil yang diminta)
            route("/users") {
                get("/me") { userService.getMe(call) }
                put("/me") { userService.putMe(call) } // Edit Nama & About
                put("/me/password") { userService.putMyPassword(call) }
                put("/me/photo") { userService.putMyPhoto(call) } // Ganti Foto Profil
            }

            // Manajemen Artis SM Entertainment (CRUD + Search + Filter)
            route("/artists") {
                get { artistService.getAll(call) } // Support Search, Filter, Pagination
                get("/stats") { artistService.getStats(call) } // Untuk Dashboard Home
                post { artistService.post(call) }
                get("/{id}") { artistService.getById(call) }
                put("/{id}") { artistService.put(call) }
                put("/{id}/image") { artistService.putImage(call) } // Upload Foto Artis
                delete("/{id}") { artistService.delete(call) }
            }
        }

        // --- PUBLIC IMAGE ROUTES ---
        route("/images") {
            get("/users/{id}") { userService.getPhoto(call) }
            get("/artists/{id}") { artistService.getImage(call) }
        }
    }
}