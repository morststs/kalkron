package click.e17.kalkron.domain.scientific

/**
 * 字句解析: 文字列をトークンのリストに分ける。
 *
 * アプリの画面ではキーが直接トークンを作るので、この処理は主にテストで
 * 式を文字列のまま書けるようにするために使う。
 */
object Lexer {

    // 長い名前から順に照合する（"sinh⁻¹(" を "sin(" より先に見る）
    private val FUNCTION_PREFIXES: List<Pair<String, MathFunction>> =
        MathFunction.entries
            .map { "${it.label}(" to it }
            .sortedByDescending { it.first.length }

    // 複数文字の記号
    private val WORD_SYMBOLS: List<Pair<String, Symbol>> =
        Symbol.entries.filter { it.text.length > 1 }.map { it.text to it }

    // 1文字の記号。テストで書きやすいよう ASCII の - * / も受け付ける
    private val CHAR_SYMBOLS: Map<Char, Symbol> =
        Symbol.entries.filter { it.text.length == 1 }.associateBy { it.text[0] } +
            mapOf('-' to Symbol.MINUS, '*' to Symbol.TIMES, '/' to Symbol.DIVIDE)

    fun tokenize(source: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < source.length) {
            val c = source[i]
            when {
                c.isWhitespace() -> i++

                c.isDigit() || c == '.' -> {
                    val start = i
                    while (i < source.length && (source[i].isDigit() || source[i] == '.')) i++
                    val digits = source.substring(start, i)
                    if (digits.count { it == '.' } > 1) throw CalcException(CalcError.SYNTAX)
                    tokens += Token.Num(digits)
                }

                else -> {
                    val function = FUNCTION_PREFIXES.firstOrNull { source.startsWith(it.first, i) }
                    val word = WORD_SYMBOLS.firstOrNull { source.startsWith(it.first, i) }
                    when {
                        function != null -> {
                            tokens += Token.Fn(function.second)
                            i += function.first.length
                        }

                        word != null -> {
                            tokens += Token.Sym(word.second)
                            i += word.first.length
                        }

                        else -> {
                            val symbol = CHAR_SYMBOLS[c] ?: throw CalcException(CalcError.SYNTAX)
                            tokens += Token.Sym(symbol)
                            i++
                        }
                    }
                }
            }
        }
        return tokens
    }
}
