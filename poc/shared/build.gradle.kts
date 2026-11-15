// 폰 앱과 차량 앱이 같이 쓰는 코드: 메시지 코덱(HELLO/FRAME/LIGHT/PING), LightingParams JSON, 켈빈→ARGB 변환, 포트·상수.
// 계약은 plan/spec.md 2절. 외부 의존성 없음(JSON 은 플랫폼 org.json). 의존성 추가는 이미지 재빌드가 필요하다(.sprintify/agent.Dockerfile).
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.hdhollow.studio.shared"
    compileSdk = 35

    defaultConfig { minSdk = 28 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.org.json) // JVM 테스트용 실제 org.json. 안드로이드 스텁 android.jar 은 호출 시 예외를 던진다
}
