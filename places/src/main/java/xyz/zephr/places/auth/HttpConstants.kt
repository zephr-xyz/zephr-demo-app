package xyz.zephr.places.auth

/**
 * Constants for HTTP-related values used across the network/auth layer.
 */
object HttpConstants {
    // HTTP Headers
    const val AUTHORIZATION_HEADER = "Authorization"
    const val BEARER_PREFIX = "Bearer "

    // HTTP Status Codes
    const val UNAUTHORIZED_CODE = 401

    // Token fetch retry/backoff
    const val TOKEN_FETCH_MAX_RETRIES = 3
    const val TOKEN_FETCH_BACKOFF_INITIAL_MS = 500L
    const val TOKEN_FETCH_BACKOFF_MAX_MS = 4000L
    const val TOKEN_FETCH_BACKOFF_MULTIPLIER = 2
}
