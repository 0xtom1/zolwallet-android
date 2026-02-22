plugins {
    id("com.android.library")
    kotlin("android")
    id("secant.android-build-conventions")
}

android {
    namespace = "xyz.zolapp.market"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    api(libs.bundles.koin)

    // Ktor for Helius DAS API calls via zol-rpc-proxy
    implementation(libs.ktor.core)
    implementation(libs.ktor.okhttp)
    implementation(libs.ktor.negotiation)
    implementation(libs.ktor.json)
}
