import java.util.Properties

// 릴리스 서명(S1·S2). 저장소 밖의 키를 keystore.properties 로 가리킨다(keystore.properties.example 참고).
// 파일이 없으면 debug 키로 서명해 assembleRelease 가 항상 돈다. 제출 APK 는 반드시 진짜 키로.
val keystoreProps: Properties? = rootProject.file("keystore.properties").takeIf { it.exists() }?.let { f ->
    Properties().apply { f.inputStream().use(::load) }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.hdhollow.studio.car"
    compileSdk = 35
    useLibrary("android.car") // 기어(GEAR_SELECTION) 읽기용 AAOS Car API

    defaultConfig {
        applicationId = "com.hdhollow.studio.car"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "0.2.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    signingConfigs {
        keystoreProps?.let { p ->
            create("release") {
                storeFile = rootProject.file(p.getProperty("storeFile"))
                storePassword = p.getProperty("storePassword")
                keyAlias = p.getProperty("keyAlias")
                keyPassword = p.getProperty("keyPassword")
                enableV2Signing = true
                enableV3Signing = true // 공모전 요건: 사이닝 키 로테이션·리니지(v3)
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false // Pleos SDK(AIDL)·리플렉션 보호. PoC 규모라 축소 이득도 없다
            signingConfig = if (keystoreProps != null) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(project(":shared")) // 폰↔차 계약: 메시지 코덱, 조명 값, 색 변환 (plan/work-plan.md 2절)
    implementation(libs.vehicle.sdk)
    implementation(libs.car.ui.lib) // FocusArea·rotate-directly: 다이얼 회전을 앱으로 받기 위해
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
}
