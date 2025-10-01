package xyz.zephr.demo.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import xyz.zephr.demo.BuildConfig
import xyz.zephr.demo.data.local.AuthTokenStore
import xyz.zephr.demo.data.network.constants.HttpConstants
import xyz.zephr.demo.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val tokenStore: AuthTokenStore
) : AuthRepository {

    override suspend fun authenticate(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: signInWithEmailPassword()
            ?: return Result.failure(Exception("Firebase sign-in failed"))

            val token = fetchIdTokenWithRetry(user, forceRefresh = true)
                ?: return Result.failure(Exception("Failed to fetch Firebase ID token"))

            tokenStore.saveToken(token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Authentication failed: ${e.message}"))
        }
    }

    override fun isAuthenticated(): Boolean {
        return tokenStore.getToken() != null && firebaseAuth.currentUser != null
    }

    override fun logout() {
        tokenStore.clearToken()
        firebaseAuth.signOut()
    }

    private suspend fun fetchIdTokenWithRetry(
        user: FirebaseUser,
        forceRefresh: Boolean,
        maxRetries: Int = HttpConstants.TOKEN_FETCH_MAX_RETRIES
    ): String? {
        var delayMs = HttpConstants.TOKEN_FETCH_BACKOFF_INITIAL_MS
        repeat(maxRetries) { attempt ->
            try {
                val token = user.getIdToken(forceRefresh).await()?.token
                if (!token.isNullOrBlank()) {
                    return token
                }
            } catch (_: Exception) {
                // Ignore and retry with backoff
            }

            if (attempt == maxRetries - 1) {
                return null
            }

            delay(delayMs)
            delayMs = (delayMs * HttpConstants.TOKEN_FETCH_BACKOFF_MULTIPLIER)
                .coerceAtMost(HttpConstants.TOKEN_FETCH_BACKOFF_MAX_MS)
        }
        return null
    }

    private suspend fun signInWithEmailPassword(): FirebaseUser? {
        val email = BuildConfig.API_USERNAME
        val password = BuildConfig.API_PASSWORD

        if (email.isBlank() || password.isBlank()) {
            return null
        }

        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return result.user
    }
}
