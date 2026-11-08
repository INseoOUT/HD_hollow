plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.hdhollow.studio.phone"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hdhollow.studio.phone"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-poc"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":shared")) // 폰↔차 계약: 메시지 코덱, 조명 값, 색 변환 (plan/work-plan.md 2절)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
}
