package xyz.zephr.demo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import xyz.zephr.places.auth.AuthRepository
import javax.inject.Inject

@HiltAndroidApp
class ZephrDemoApplication : Application() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            try {
                if (!authRepository.isAuthenticated()) {
                    authRepository.authenticate()
                }
            } catch (_: Exception) {
                // Ignore startup failures; they will be retried by interceptor when needed
            }
        }
    }
}