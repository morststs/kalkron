package click.e17.kalkron.domain.programmer

/**
 * 字句解析: 文字列をトークンのリストに分ける。
 *
 * アプリの画面ではキーが直接トークンを作るので、この処理は主にテストで
 * 式を文字列のまま書けるようにするために使う。
 */
object ProgLexer {

    // 複数文字の記号。HEX の A〜F と衝突しないよう、数字より先に照合する。長いものから順に見る
    private val WORDS: List<Pair<String, ProgSymbol>> =
        ProgSymbol.entries
            .filter { it.text.length > 1 }
            .map { it.text to it }
            .sortedByDescending { it.first.length }

    // 1文字の記号。テストで書きやすいよう ASCII の - * / も受け付ける
    private val CHARS: Map<Char, ProgSymbol> =
        ProgSymbol.entries.filter { it.text.length == 1 }.associateBy { it.text[0] } +
            mapOf('-' to ProgSymbol.MINUS, '*' to ProgSymbol.TIMES, '/' to ProgSymbol.DIVIDE)

    fun tokenize(
        source: String,
        radix: Radix = Radix.DEC,
        wordSize: WordSize = WordSize.QWORD,
        signed: Boolean = true,
    ): List<ProgToken> {
        val tokens = mutableListOf<ProgToken>()
        var i = 0
        while (i < source.length) {
            val c = source[i]
            if (c.isWhitespace()) {
                i++
                continue
            }

            val word = WORDS.firstOrNull { source.startsWith(it.first, i) }
            if (word != null) {
                tokens += ProgToken.Sym(word.second)
                i += word.first.length
                continue
            }

            val start = i
            while (i < source.length && radix.accepts(digitValue(source[i]))) i++
            if (i > start) {
                val value = try {
                    java.lang.Long.parseUnsignedLong(source.substring(start, i), radix.base)
                } catch (e: NumberFormatException) {
                    throw ProgException(ProgError.SYNTAX)
                }
                tokens += ProgToken.Num(wordSize.normalize(value, signed))
                continue
            }

            val symbol = CHARS[c] ?: throw ProgException(ProgError.SYNTAX)
            tokens += ProgToken.Sym(symbol)
            i++
        }
        return tokens
    }

    /** 数字1文字の値（0〜15）。数字でなければ −1 */
    private fun digitValue(c: Char): Int = when (c) {
        in '0'..'9' -> c - '0'
        in 'A'..'F' -> c - 'A' + 10
        else -> -1
    }
}
