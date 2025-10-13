import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "xyz.zephr.places"
    compileSdk = 36

    defaultConfig {
        minSdk = 31
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
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.play.services.maps)

    implementation(libs.retrofit)
    implementation(libs.gson)
}

repositories {
    google()
    mavenCentral()
}
