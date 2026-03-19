package org.delcom.module

import org.delcom.repositories.*
import org.delcom.services.AuthService
import org.delcom.services.ArtistService
import org.delcom.services.UserService
import org.koin.dsl.module

fun appModule(jwtSecret: String) = module {

    // --- 1. User & Auth Management ---
    single<IUserRepository> { UserRepository() }
    single<IRefreshTokenRepository> { RefreshTokenRepository() }

    single { UserService(get(), get()) }
    single { AuthService(jwtSecret, get(), get()) }

    // --- 2. Artist & Entertainment Repositories ---
    single<IArtistRepository> { ArtistRepository() }

    // Tambahkan Repository Baru untuk Fitur Unggulan
    single<IAlbumRepository> { AlbumRepository() }     // Untuk Interactive Discography
    single<IFavoriteRepository> { FavoriteRepository() } // Untuk Bias System

    // --- 3. Artist Service (Update dengan 4 Dependency) ---
    single {
        // ArtistService sekarang membutuhkan: userRepo, artistRepo, albumRepo, favoriteRepo
        ArtistService(get(), get(), get(), get())
    }
}