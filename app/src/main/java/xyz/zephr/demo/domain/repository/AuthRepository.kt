package xyz.zephr.demo.domain.repository

interface AuthRepository {
    suspend fun authenticate(): Result<Unit>
    fun isAuthenticated(): Boolean
    fun logout()
}
