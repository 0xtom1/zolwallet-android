import java.util.Properties

plugins {
    id("com.android.library")
    kotlin("android")
    id("secant.android-build-conventions")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    namespace = "xyz.zolapp.solana"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField(
            "String",
            "HELIUS_API_KEY",
            "\"${localProperties.getProperty("HELIUS_API_KEY", "")}\""
        )
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    api(libs.bundles.koin)

    // Sol4k - Solana SDK for Kotlin
    api("org.sol4k:sol4k:${project.property("SOL4K_VERSION")}") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    }
}
