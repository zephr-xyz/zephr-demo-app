package xyz.zephr.places.auth

interface AuthRepository {
    suspend fun authenticate(): Result<Unit>
    fun isAuthenticated(): Boolean
    fun logout()
}
