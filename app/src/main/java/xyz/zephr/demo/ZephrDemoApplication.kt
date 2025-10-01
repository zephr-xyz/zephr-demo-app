package xyz.zephr.demo

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import xyz.zephr.demo.domain.repository.AuthRepository
import javax.inject.Inject

@HiltAndroidApp
class ZephrDemoApplication : Application() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
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