import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.hilt.android)
    kotlin("kapt")
}

val secretsFile = rootProject.file("secrets.properties")
val secrets = Properties().apply {
    require(secretsFile.exists()) {
        "Missing secrets.properties file at ${secretsFile.absolutePath}"
    }
    secretsFile.inputStream().use { load(it) }
}

android {
    namespace = "xyz.zephr.places"
    compileSdk = 36

    defaultConfig {
        minSdk = 31
        val rawApiBaseUrl = secrets.getProperty("API_BASE_URL")
            ?: throw GradleException("Missing API_BASE_URL entry in secrets.properties")
        val sanitizedBaseUrl = rawApiBaseUrl.trim().trimEnd('/')
        val normalizedBaseUrl = if (sanitizedBaseUrl.endsWith("/v1")) {
            "$sanitizedBaseUrl/"
        } else {
            "$sanitizedBaseUrl/v1/"
        }
        buildConfigField("String", "API_BASE_URL", "\"$normalizedBaseUrl\"")
        val apiUsername = secrets.getProperty("API_USERNAME")
            ?: throw GradleException("Missing API_USERNAME entry in secrets.properties")
        val apiPassword = secrets.getProperty("API_PASSWORD")
            ?: throw GradleException("Missing API_PASSWORD entry in secrets.properties")
        buildConfigField("String", "API_USERNAME", "\"$apiUsername\"")
        buildConfigField("String", "API_PASSWORD", "\"$apiPassword\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.play.services.maps)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.security.crypto)
}

repositories {
    google()
    mavenCentral()
}
