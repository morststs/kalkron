package click.e17.kalkron.domain

/**
 * 計算をどのモードで行ったか。履歴の区分に使う。
 * DB には名前（name）の文字列で保存するため、既存の値の名前は変えないこと。
 */
enum class CalculatorMode {
    STANDARD,
    SCIENTIFIC,
    PROGRAMMER,
}
