package click.e17.kalkron.domain.scientific

/**
 * 構文解析: トークン列を構文木にする（再帰下降法）。
 *
 * 文法（優先順位の低い順）:
 * ```
 * 式     := 項 (('+' | '−') 項)*
 * 項     := 単項 (('×' | '÷') 単項 | 暗黙の掛け算 単項)*
 * 単項   := '−' 単項 | 累乗
 * 累乗   := 後置 ('^' 単項)?
 * 後置   := 基本 '²'*
 * 基本   := 数値 | x | π | e | ANS | M | 関数 '(' 式 (',' 式)* ')' | '(' 式 ')'
 * ```
 * 文法の1行が1つの関数に対応している。
 */
object Parser {

    fun parse(tokens: List<Token>): Expr {
        if (tokens.isEmpty()) throw syntaxError()
        val cursor = Cursor(tokens)
        val expr = cursor.expression()
        // 式を読み終えたのにトークンが残っていれば、余計なものが付いている
        if (!cursor.isAtEnd) throw syntaxError()
        return expr
    }

    private fun syntaxError() = CalcException(CalcError.SYNTAX)

    /** トークン列の読み取り位置を持ちながら、文法の各規則を読む */
    private class Cursor(private val tokens: List<Token>) {
        private var position = 0

        val isAtEnd: Boolean get() = position == tokens.size

        // 式 := 項 (('+' | '−') 項)*
        fun expression(): Expr {
            var left = term()
            while (true) {
                left = when {
                    match(Symbol.PLUS) -> Expr.Binary(BinaryOp.ADD, left, term())
                    match(Symbol.MINUS) -> Expr.Binary(BinaryOp.SUBTRACT, left, term())
                    else -> return left
                }
            }
        }

        // 項 := 単項 (('×' | '÷') 単項 | 暗黙の掛け算 単項)*
        private fun term(): Expr {
            var left = unary()
            while (true) {
                left = when {
                    match(Symbol.TIMES) -> Expr.Binary(BinaryOp.MULTIPLY, left, unary())
                    match(Symbol.DIVIDE) -> Expr.Binary(BinaryOp.DIVIDE, left, unary())
                    // 次が「基本」の始まりなら、間に × があるとみなす（2π、3x、2(1+3)）
                    startsPrimary(peek()) -> Expr.Binary(BinaryOp.MULTIPLY, left, unary())
                    else -> return left
                }
            }
        }

        // 単項 := '−' 単項 | 累乗
        private fun unary(): Expr =
            if (match(Symbol.MINUS)) Expr.Negate(unary()) else power()

        // 累乗 := 後置 ('^' 単項)?
        // 右辺で「単項」を読むことで、右結合（2^3^2 = 2^9）と 2^−1 の両方を実現する
        private fun power(): Expr {
            val base = postfix()
            return if (match(Symbol.POWER)) Expr.Binary(BinaryOp.POWER, base, unary()) else base
        }

        // 後置 := 基本 '²'*
        private fun postfix(): Expr {
            var expr = primary()
            while (match(Symbol.SQUARE)) expr = Expr.Square(expr)
            return expr
        }

        // 基本 := 数値 | x | π | e | ANS | M | 関数 '(' 式 (',' 式)* ')' | '(' 式 ')'
        private fun primary(): Expr {
            val token = next() ?: throw syntaxError()
            return when (token) {
                is Token.Num -> Expr.Num(token.digits.toDoubleOrNull() ?: throw syntaxError())

                is Token.Fn -> {
                    val args = mutableListOf(expression())
                    while (match(Symbol.COMMA)) args += expression()
                    expect(Symbol.RIGHT_PAREN)
                    if (args.size != token.function.arity) throw syntaxError()
                    Expr.Call(token.function, args)
                }

                is Token.Sym -> when (token.symbol) {
                    Symbol.LEFT_PAREN -> expression().also { expect(Symbol.RIGHT_PAREN) }
                    Symbol.X -> Expr.Variable
                    Symbol.PI, Symbol.E, Symbol.ANS, Symbol.MEMORY -> Expr.Constant(token.symbol)
                    else -> throw syntaxError()
                }
            }
        }

        private fun startsPrimary(token: Token?): Boolean = when (token) {
            is Token.Num, is Token.Fn -> true
            is Token.Sym -> token.symbol in PRIMARY_SYMBOLS
            null -> false
        }

        private fun peek(): Token? = tokens.getOrNull(position)

        private fun next(): Token? = tokens.getOrNull(position)?.also { position++ }

        private fun match(symbol: Symbol): Boolean {
            val token = peek()
            if (token is Token.Sym && token.symbol == symbol) {
                position++
                return true
            }
            return false
        }

        private fun expect(symbol: Symbol) {
            if (!match(symbol)) throw syntaxError()
        }

        private companion object {
            val PRIMARY_SYMBOLS = setOf(
                Symbol.LEFT_PAREN, Symbol.X, Symbol.PI, Symbol.E, Symbol.ANS, Symbol.MEMORY,
            )
        }
    }
}
