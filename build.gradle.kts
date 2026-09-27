// ルートの build.gradle.kts。
// ここではプラグインの「宣言」だけを行い、実際の適用は app モジュール側で行う。
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}
