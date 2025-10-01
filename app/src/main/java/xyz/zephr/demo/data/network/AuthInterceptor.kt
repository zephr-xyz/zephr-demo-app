package xyz.zephr.demo.data.network

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import xyz.zephr.demo.data.local.AuthTokenStore
import xyz.zephr.demo.data.network.constants.HttpConstants

class AuthInterceptor(
    private val firebaseAuth: FirebaseAuth,
    private val tokenStore: AuthTokenStore,
    private val authEventBus: AuthEventBus
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()

        val initialToken = ensureToken()
        val requestWithAuth: Request = initialToken?.let { token ->
            originalRequest.newBuilder()
                .header(HttpConstants.AUTHORIZATION_HEADER, "${HttpConstants.BEARER_PREFIX}$token")
                .build()
        } ?: originalRequest

        var response: Response = chain.proceed(requestWithAuth)

        if (response.code == HttpConstants.UNAUTHORIZED_CODE) {
            response.close()

            val refreshedToken = fetchTokenWithRetry(forceRefresh = true)
            if (refreshedToken.isNullOrBlank()) {
                handleUnauthorized()
                return response
            }

            val retriedRequest = originalRequest.newBuilder()
                .header(
                    HttpConstants.AUTHORIZATION_HEADER,
                    "${HttpConstants.BEARER_PREFIX}$refreshedToken"
                )
                .build()

            response = chain.proceed(retriedRequest)

            if (response.code == HttpConstants.UNAUTHORIZED_CODE) {
                handleUnauthorized()
                return chain.proceed(originalRequest)
            }
        }

        return response
    }

    private fun ensureToken(): String? {
        val cached = tokenStore.getToken()
        if (!cached.isNullOrBlank()) {
            return cached
        }

        return fetchTokenWithRetry(forceRefresh = false)
    }

    private fun fetchTokenWithRetry(
        forceRefresh: Boolean,
        maxRetries: Int = HttpConstants.TOKEN_FETCH_MAX_RETRIES
    ): String? {
        var delayMs = HttpConstants.TOKEN_FETCH_BACKOFF_INITIAL_MS
        repeat(maxRetries) { attempt ->
            val token = runBlocking { fetchToken(forceRefresh) }
            if (!token.isNullOrBlank()) {
                tokenStore.saveToken(token)
                return token
            }

            if (attempt == maxRetries - 1) {
                return null
            }

            try {
                Thread.sleep(delayMs)
            } catch (_: InterruptedException) {
                return null
            }

            delayMs = (delayMs * HttpConstants.TOKEN_FETCH_BACKOFF_MULTIPLIER)
                .coerceAtMost(HttpConstants.TOKEN_FETCH_BACKOFF_MAX_MS)
        }

        return null
    }

    private suspend fun fetchToken(forceRefresh: Boolean): String? {
        val user = firebaseAuth.currentUser ?: return null

        return try {
            user.getIdToken(forceRefresh).await()?.token
        } catch (_: Exception) {
            null
        }
    }

    private fun handleUnauthorized() {
        tokenStore.clearToken()
        authEventBus.emitUnauthorized()
    }
}
