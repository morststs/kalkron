package click.e17.kalkron.domain.programmer

/**
 * 構文解析: トークン列を構文木にする（再帰下降法）。
 *
 * 文法（C 言語と同じ優先順位。弱い順）:
 * ```
 * 式       := XOR式   (('OR'  | 'NOR')  XOR式)*
 * XOR式    := AND式   ('XOR' AND式)*
 * AND式    := シフト式 (('AND' | 'NAND') シフト式)*
 * シフト式  := 加減式  (('<<' | '>>' | 'RoL' | 'RoR') 加減式)*
 * 加減式    := 乗除式  (('+' | '−') 乗除式)*
 * 乗除式    := 単項    (('×' | '÷' | 'MOD') 単項)*
 * 単項     := ('−' | 'NOT') 単項 | 基本
 * 基本     := 数値 | ANS | '(' 式 ')'
 * ```
 * 二項演算の6段は形が同じなので、共通の level 関数に「1段下の規則」と「この段の演算子」を渡して書く。
 */
object ProgParser {

    fun parse(tokens: List<ProgToken>): ProgExpr {
        if (tokens.isEmpty()) throw syntaxError()
        val cursor = Cursor(tokens)
        val expr = cursor.expression()
        if (!cursor.isAtEnd) throw syntaxError()
        return expr
    }

    private fun syntaxError() = ProgException(ProgError.SYNTAX)

    private val OR_OPS = mapOf(ProgSymbol.OR to ProgBinaryOp.OR, ProgSymbol.NOR to ProgBinaryOp.NOR)
    private val XOR_OPS = mapOf(ProgSymbol.XOR to ProgBinaryOp.XOR)
    private val AND_OPS = mapOf(ProgSymbol.AND to ProgBinaryOp.AND, ProgSymbol.NAND to ProgBinaryOp.NAND)
    private val SHIFT_OPS = mapOf(
        ProgSymbol.SHL to ProgBinaryOp.SHL,
        ProgSymbol.SHR to ProgBinaryOp.SHR,
        ProgSymbol.ROL to ProgBinaryOp.ROL,
        ProgSymbol.ROR to ProgBinaryOp.ROR,
    )
    private val ADD_OPS = mapOf(ProgSymbol.PLUS to ProgBinaryOp.ADD, ProgSymbol.MINUS to ProgBinaryOp.SUBTRACT)
    private val MUL_OPS = mapOf(
        ProgSymbol.TIMES to ProgBinaryOp.MULTIPLY,
        ProgSymbol.DIVIDE to ProgBinaryOp.DIVIDE,
        ProgSymbol.MOD to ProgBinaryOp.MOD,
    )

    private class Cursor(private val tokens: List<ProgToken>) {
        private var position = 0

        val isAtEnd: Boolean get() = position == tokens.size

        // 式 := XOR式 (('OR' | 'NOR') XOR式)*
        fun expression(): ProgExpr = level(::xorLevel, OR_OPS)

        // XOR式 := AND式 ('XOR' AND式)*
        private fun xorLevel(): ProgExpr = level(::andLevel, XOR_OPS)

        // AND式 := シフト式 (('AND' | 'NAND') シフト式)*
        private fun andLevel(): ProgExpr = level(::shiftLevel, AND_OPS)

        // シフト式 := 加減式 (('<<' | '>>' | 'RoL' | 'RoR') 加減式)*
        private fun shiftLevel(): ProgExpr = level(::additive, SHIFT_OPS)

        // 加減式 := 乗除式 (('+' | '−') 乗除式)*
        private fun additive(): ProgExpr = level(::multiplicative, ADD_OPS)

        // 乗除式 := 単項 (('×' | '÷' | 'MOD') 単項)*
        private fun multiplicative(): ProgExpr = level(::unary, MUL_OPS)

        /** 「next (演算子 next)*」の形を読む。左から順に結び付ける（左結合） */
        private fun level(next: () -> ProgExpr, ops: Map<ProgSymbol, ProgBinaryOp>): ProgExpr {
            var left = next()
            while (true) {
                val op = (peek() as? ProgToken.Sym)?.symbol?.let { ops[it] } ?: return left
                position++
                left = ProgExpr.Binary(op, left, next())
            }
        }

        // 単項 := ('−' | 'NOT') 単項 | 基本
        private fun unary(): ProgExpr = when {
            match(ProgSymbol.MINUS) -> ProgExpr.Unary(ProgUnaryOp.NEGATE, unary())
            match(ProgSymbol.NOT) -> ProgExpr.Unary(ProgUnaryOp.NOT, unary())
            else -> primary()
        }

        // 基本 := 数値 | ANS | '(' 式 ')'
        private fun primary(): ProgExpr {
            val token = tokens.getOrNull(position)?.also { position++ } ?: throw syntaxError()
            return when (token) {
                is ProgToken.Num -> ProgExpr.Num(token.value)
                is ProgToken.Sym -> when (token.symbol) {
                    ProgSymbol.ANS -> ProgExpr.Ans
                    ProgSymbol.LEFT_PAREN -> expression().also { expect(ProgSymbol.RIGHT_PAREN) }
                    else -> throw syntaxError()
                }
            }
        }

        private fun peek(): ProgToken? = tokens.getOrNull(position)

        private fun match(symbol: ProgSymbol): Boolean {
            val token = peek()
            if (token is ProgToken.Sym && token.symbol == symbol) {
                position++
                return true
            }
            return false
        }

        private fun expect(symbol: ProgSymbol) {
            if (!match(symbol)) throw syntaxError()
        }
    }
}
