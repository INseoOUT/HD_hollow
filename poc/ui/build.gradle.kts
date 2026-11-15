// 폰 앱과 차량 앱이 같이 쓰는 디자인 시스템: 팔레트·글자 크기(StudioTheme), 그라데이션 슬라이더, 칩·무드 색·상태 표시, 조명 값 라벨.
// 계약(:shared)과는 분리한다. :shared 는 Compose 를 모르고, 이 모듈은 전송·메시지를 모른다.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.hdhollow.studio.ui"
    compileSdk = 35

    defaultConfig { minSdk = 28 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":shared")) // LightingParams·ColorTemp(켈빈→색)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
}
