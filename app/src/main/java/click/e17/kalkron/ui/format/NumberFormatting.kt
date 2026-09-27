package click.e17.kalkron.ui.format

/**
 * 画面表示用の整形。
 *
 * 計算そのものは [click.e17.kalkron.domain.CalculatorEngine] が扱う文字列で行い、
 * 3 桁区切りは「見せるときだけ」ここで足す。内部状態にカンマを混ぜると
 * 数値への変換や桁数の判定が壊れるため、両者は必ず分けておく。
 */

/** 数値の整数部に 3 桁区切りを入れる。"1234.5" → "1,234.5" */
fun groupDigits(text: String): String {
    if (text.isEmpty()) return text

    // 指数表記（1.0E+20）やエラー文字など、数字・符号・小数点以外を含むものは触らない
    if (text.any { !it.isDigit() && it != '-' && it != '.' }) return text

    val negative = text.startsWith("-")
    val body = if (negative) text.drop(1) else text

    val dot = body.indexOf('.')
    val integerPart = if (dot >= 0) body.take(dot) else body
    // "." だけの場合も、小数部がある場合もそのまま後ろに付ける（入力途中の "12." を壊さない）
    val rest = if (dot >= 0) body.substring(dot) else ""

    if (integerPart.isEmpty()) return text

    val grouped = integerPart
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()

    return buildString {
        if (negative) append('-')
        append(grouped)
        append(rest)
    }
}

/** 式の中に現れる数値だけを 3 桁区切りにする。"1024 + 48.5" → "1,024 + 48.5" */
fun groupExpression(text: String): String =
    NUMBER_PATTERN.replace(text) { match -> groupDigits(match.value) }

// 演算子には全角の記号（+ − × ÷）を使っているため、ASCII の "-" は符号としてのみ現れる
private val NUMBER_PATTERN = Regex("""-?\d+(\.\d+)?""")
