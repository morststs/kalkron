import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    // AGP 9 以降は Kotlin サポートが AGP に内蔵されたため、
    // org.jetbrains.kotlin.android プラグインを適用してはいけない
    alias(libs.plugins.android.application)
    // Compose Compiler Gradle プラグイン（Kotlin 2.0 以降はこれが必須）
    alias(libs.plugins.kotlin.compose)
    // Navigation の型安全ルート（@Serializable）で使用
    alias(libs.plugins.kotlin.serialization)
    // Room のコード生成に使用（kapt ではなく KSP）
    alias(libs.plugins.ksp)
}

// リリース署名の情報は keystore.properties から読む。
// このファイルはリポジトリに含めないため、持っていない人がクローンしても
// ビルド自体は通る（その場合 release は未署名になる）。
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        FileInputStream(keystorePropertiesFile).use { load(it) }
    }
}

android {
    // R クラスや BuildConfig が生成されるパッケージ名
    namespace = "click.e17.kalkron"
    compileSdk = 37

    defaultConfig {
        applicationId = "click.e17.kalkron"
        minSdk = 26
        targetSdk = 37
        versionCode = 5
        versionName = "1.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")

                // minSdk 26 では AGP が v1（JAR）署名を省くが、
                // 一部のエミュレータや改変された Android は v1 を要求するため明示的に付ける
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    signingConfigs.getByName("debug") {
        // デバッグ APK も同じ理由で v1 署名を付ける
        enableV1Signing = true
        enableV2Signing = true
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            // 難読化は無効のまま。端末で動作確認できない環境なので、
            // R8 による削除が原因の実行時エラーを避ける
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        // Jetpack Compose を有効化する
        compose = true
    }

    sourceSets {
        getByName("androidTest") {
            // マイグレーションのテストで、書き出したスキーマ（schemas/*.json）を読めるようにする
            assets.srcDir("$projectDir/schemas")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

ksp {
    // Room のスキーマ（DB 定義の JSON）を書き出す先。マイグレーション時に役立つ
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // --- Android / Kotlin 基本 ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --- Compose ---
    // BOM を使うと個々の Compose ライブラリのバージョン指定が不要になる
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    // collectAsStateWithLifecycle / viewModel() を使うため
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // --- 画面遷移 ---
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // --- Room（ローカル DB） ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // --- ユニットテスト（JVM 上で動く。端末不要） ---
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // --- 計装テスト（端末・エミュレータ上で動く） ---
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)

    // --- デバッグビルド専用（プレビュー・レイアウト検査用） ---
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
