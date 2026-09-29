package click.e17.kalkron.domain.scientific

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * 関数電卓の表示用の整形。3桁区切りは UI 層で入れるので、ここでは入れない。
 */
object ScientificFormat {

    private val CONTEXT = MathContext(12, RoundingMode.HALF_UP)
    private val LARGE = BigDecimal("1e15")
    private val SMALL = BigDecimal("1e-6")

    /** 結果を有効桁数12桁の文字列にする。大きすぎる・小さすぎる値は指数表記 */
    fun number(value: Double): String {
        if (value == 0.0) return "0"
        val rounded = BigDecimal(value).round(CONTEXT)
        val magnitude = rounded.abs()
        return if (magnitude >= LARGE || magnitude < SMALL) {
            val stripped = rounded.stripTrailingZeros()
            // 仮数部を 1 以上 10 未満にするための桁のずれ
            val exponent = stripped.precision() - stripped.scale() - 1
            val mantissa = stripped.movePointLeft(exponent).toPlainString()
            val sign = if (exponent >= 0) "+" else ""
            "${mantissa}E$sign$exponent"
        } else {
            val stripped = rounded.stripTrailingZeros()
            // stripTrailingZeros は 100 を 1E+2 にするので、整数に戻してから文字列化する
            val normalized = if (stripped.scale() < 0) stripped.setScale(0) else stripped
            normalized.toPlainString()
        }
    }

    /** トークン列を画面に出す式の文字列にする */
    fun expression(tokens: List<Token>): String = buildString {
        tokens.forEachIndexed { index, token ->
            when {
                token is Token.Sym && token.symbol == Symbol.COMMA -> append(", ")
                token is Token.Sym && token.symbol in SPACED_OPERATORS &&
                    !isUnaryMinus(token, tokens.getOrNull(index - 1)) ->
                    append(" ").append(token.text).append(" ")
                else -> append(token.text)
            }
        }
    }.trim()

    private val SPACED_OPERATORS = setOf(Symbol.PLUS, Symbol.MINUS, Symbol.TIMES, Symbol.DIVIDE)

    private val BEFORE_UNARY = setOf(
        Symbol.PLUS, Symbol.MINUS, Symbol.TIMES, Symbol.DIVIDE, Symbol.POWER,
        Symbol.LEFT_PAREN, Symbol.COMMA,
    )

    /** 直前のトークンから、この − が単項マイナス（符号）かどうかを判定する */
    private fun isUnaryMinus(token: Token.Sym, previous: Token?): Boolean =
        token.symbol == Symbol.MINUS && (
            previous == null ||
                previous is Token.Fn ||
                (previous is Token.Sym && previous.symbol in BEFORE_UNARY)
            )
}
