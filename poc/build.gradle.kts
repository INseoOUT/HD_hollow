plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false // :shared. application 과 같은 AGP 라 루트에서 같이 선언해야 버전 충돌이 안 난다
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
