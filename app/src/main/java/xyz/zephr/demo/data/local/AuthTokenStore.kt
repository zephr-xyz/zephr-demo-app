package xyz.zephr.demo.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import xyz.zephr.demo.BuildConfig

class AuthTokenStore(context: Context) {

    private val preferences: SharedPreferences = createEncryptedPrefs(context)

    fun saveToken(token: String) {
        if (BuildConfig.DEBUG) {
            Log.d("AuthTokenStore", "Saved token: $token")
        }
        preferences.edit { putString(KEY_TOKEN, token) }
    }

    fun getToken(): String? = preferences.getString(KEY_TOKEN, null)

    fun clearToken() {
        preferences.edit { remove(KEY_TOKEN) }
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_TOKEN = "access_token"

        private fun createEncryptedPrefs(context: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            return EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
}
