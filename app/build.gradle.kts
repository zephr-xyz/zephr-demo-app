import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.secrets.gradle.plugin)
    alias(libs.plugins.hilt.android)
    kotlin("kapt")
    alias(libs.plugins.google.services)
}

android {
    namespace = "xyz.zephr.demo"
    compileSdk = 36

    defaultConfig {
        applicationId = "xyz.zephr.demo"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "0.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val secretsFile = rootProject.file("secrets.properties")
    val secrets = Properties().apply {
        require(secretsFile.exists()) {
            "Missing secrets.properties file at ${secretsFile.absolutePath}"
        }
        secretsFile.inputStream().use { load(it) }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"${secrets.getProperty("API_BASE_URL")}\"")
            buildConfigField("String", "API_USERNAME", "\"${secrets.getProperty("API_USERNAME")}\"")
            buildConfigField("String", "API_PASSWORD", "\"${secrets.getProperty("API_PASSWORD")}\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"${secrets.getProperty("API_BASE_URL")}\"")
            buildConfigField("String", "API_USERNAME", "\"${secrets.getProperty("API_USERNAME")}\"")
            buildConfigField("String", "API_PASSWORD", "\"${secrets.getProperty("API_PASSWORD")}\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
        compose = true
        buildConfig = true
    }
}

repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://us-central1-maven.pkg.dev/zephr-xyz-firebase-development/maven-repo")
        credentials {
            username = "_json_key_base64"
            password = findProperty("zephr_maven_repo.password") as String?
                ?: throw GradleException("Missing required gradle property needed to access zephr maven repo: 'zephr_maven_repo.password'")
        }
        authentication {
            create<BasicAuthentication>("basic")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Compose Material Icons
    implementation(libs.androidx.material.icons.extended)

    // Maps compose
    implementation(libs.maps.compose)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.compiler)

    // Permissions
    implementation(libs.accompanist.permissions)

    // Network
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.gson)

    implementation(libs.zephr.sdk) {
        isChanging = true
    }

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.androidx.security.crypto)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}

secrets {
    // Change the properties file from the default "local.properties" in your root project
    // to another properties file in your root project.
    propertiesFileName = "secrets.properties"
}
