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

    /**
     * トークン列を画面に出す式の文字列にする。
     *
     * @param values 値に置き換える記号（ANS・M）とその値。履歴に残すときに渡す。
     *   後から履歴を見たとき、ANS や M が何の値だったか分からなくなるため
     */
    fun expression(tokens: List<Token>, values: Map<Symbol, Double> = emptyMap()): String = buildString {
        tokens.forEachIndexed { index, token ->
            val value = (token as? Token.Sym)?.symbol?.let { values[it] }
            when {
                value != null -> append(valueText(value, tokens.getOrNull(index - 1), tokens.getOrNull(index + 1)))
                token is Token.Sym && token.symbol == Symbol.COMMA -> append(", ")
                token is Token.Sym && token.symbol in SPACED_OPERATORS &&
                    !isUnaryMinus(token, tokens.getOrNull(index - 1)) ->
                    append(" ").append(token.text).append(" ")
                else -> append(token.text)
            }
        }
    }.trim()

    /**
     * ANS・M を置き換える値の文字列。負の値や、暗黙の掛け算で隣と続けて読めてしまう位置では括弧で囲む
     * （2ANS の ANS が 5 のとき、25 ではなく 2(5) とする）。
     */
    private fun valueText(value: Double, previous: Token?, next: Token?): String {
        // 符号は式の単項マイナスと同じ − にする（指数表記の E-20 の - はそのまま）
        val text = if (value < 0) "−" + number(-value) else number(value)
        val needsParens = value < 0 || isImplicitLeft(previous) || isImplicitRight(next)
        return if (needsParens) "($text)" else text
    }

    /** 直後の値と暗黙の掛け算になるトークン（数・定数・閉じ括弧・²） */
    private fun isImplicitLeft(token: Token?): Boolean =
        token is Token.Num || (token is Token.Sym && token.symbol in VALUE_END)

    /** 直前の値と暗黙の掛け算になるトークン（数・定数・関数・開き括弧） */
    private fun isImplicitRight(token: Token?): Boolean =
        token is Token.Num || token is Token.Fn || (token is Token.Sym && token.symbol in VALUE_START)

    private val VALUE_START = setOf(Symbol.X, Symbol.PI, Symbol.E, Symbol.ANS, Symbol.MEMORY, Symbol.LEFT_PAREN)
    private val VALUE_END = setOf(Symbol.X, Symbol.PI, Symbol.E, Symbol.ANS, Symbol.MEMORY, Symbol.RIGHT_PAREN, Symbol.SQUARE)

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
