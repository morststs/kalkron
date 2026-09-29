package click.e17.kalkron.domain.programmer

/**
 * プログラマーモードの表示用の整形。
 */
object ProgFormat {

    /**
     * トークン列を画面に出す式の文字列にする。数値は今の基数で、区切りなしで表示する。
     * 二項演算子の前後に空白を入れる。単項の − には入れず、NOT の後ろには入れる（NOTFF と読めなくなるため）。
     */
    fun expression(tokens: List<ProgToken>, radix: Radix, wordSize: WordSize, signed: Boolean): String =
        buildString {
            tokens.forEachIndexed { index, token ->
                when (token) {
                    is ProgToken.Num -> append(radix.plain(token.value, wordSize, signed))
                    is ProgToken.Sym -> when (token.symbol) {
                        ProgSymbol.LEFT_PAREN, ProgSymbol.RIGHT_PAREN, ProgSymbol.ANS -> append(token.symbol.text)
                        ProgSymbol.NOT -> append("NOT ")
                        ProgSymbol.MINUS ->
                            if (isUnaryPosition(tokens.getOrNull(index - 1))) append("−") else append(" − ")
                        else -> append(" ").append(token.symbol.text).append(" ")
                    }
                }
            }
        }.replace("  ", " ").trim()

    /** 履歴に保存する結果。値に基数名を付ける（例: "F (HEX)"） */
    fun result(value: Long, radix: Radix, wordSize: WordSize, signed: Boolean): String =
        "${radix.grouped(value, wordSize, signed)} (${radix.label})"

    /** 直前のトークンから、この − が単項（符号）かどうかを判定する */
    private fun isUnaryPosition(previous: ProgToken?): Boolean =
        previous == null ||
            (previous is ProgToken.Sym && previous.symbol != ProgSymbol.RIGHT_PAREN && previous.symbol != ProgSymbol.ANS)
}
