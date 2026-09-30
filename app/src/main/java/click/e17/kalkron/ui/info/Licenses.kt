package click.e17.kalkron.ui.info

/**
 * アプリに含まれるフォント・ライブラリと、そのライセンス。
 *
 * @param licenseAsset ライセンス全文のファイル（assets/ からの相対パス）
 */
data class LicenseEntry(
    val name: String,
    val detail: String,
    val licenseName: String,
    val licenseAsset: String,
)

private const val APACHE = "Apache License 2.0"
private const val APACHE_TEXT = "licenses/Apache-2.0.txt"
private const val OFL = "SIL Open Font License 1.1"

/** 情報画面に並べる一覧。依存ライブラリを増やしたらここにも足す */
val LICENSES: List<LicenseEntry> = listOf(
    // このアプリ自身。全文はリポジトリ直下の LICENSE の写し（一致することを LicensesTest で確かめている）
    LicenseEntry("Kalkron", "このアプリ", "MIT License", "licenses/Kalkron-MIT.txt"),
    // 同梱フォント。OFL はフォントを配布するときにライセンスを添えることを求めている
    LicenseEntry("JetBrains Mono", "数字・キー・記号の等幅フォント", OFL, "licenses/JetBrainsMono-OFL.txt"),
    LicenseEntry("Space Grotesk", "見出しのフォント", OFL, "licenses/SpaceGrotesk-OFL.txt"),
    LicenseEntry("Geist", "本文のフォント", OFL, "licenses/Geist-OFL.txt"),
    // ライブラリ（すべて Apache License 2.0）
    LicenseEntry("Android Jetpack", "Compose / Material3 / Room / Navigation / Lifecycle / Activity / Core", APACHE, APACHE_TEXT),
    LicenseEntry("Material Icons", "画面のアイコン", APACHE, APACHE_TEXT),
    LicenseEntry("Kotlin", "標準ライブラリ / Coroutines / Serialization", APACHE, APACHE_TEXT),
)
