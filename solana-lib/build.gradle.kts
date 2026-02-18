plugins {
    id("com.android.library")
    kotlin("android")
    id("secant.android-build-conventions")
}

android {
    namespace = "xyz.zolapp.solana"
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
