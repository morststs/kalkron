package click.e17.kalkron.domain.programmer

/**
 * 評価に必要な外部の値。語長・符号の有無・直前の結果（ANS）。
 */
data class ProgContext(
    val wordSize: WordSize = WordSize.QWORD,
    val signed: Boolean = true,
    val ans: Long = 0L,
)

/**
 * 評価器: 構文木をたどって整数を計算する。
 *
 * 各ノードの結果を必ず語長で正規化するので、途中の値も最終結果も
 * 「その語長のレジスタで計算した値」と一致する。
 */
object ProgEvaluator {

    fun evaluate(expr: ProgExpr, context: ProgContext = ProgContext()): Long = eval(expr, context)

    private fun eval(expr: ProgExpr, context: ProgContext): Long {
        val raw = when (expr) {
            is ProgExpr.Num -> expr.value
            ProgExpr.Ans -> context.ans
            is ProgExpr.Unary -> {
                val v = eval(expr.operand, context)
                when (expr.op) {
                    ProgUnaryOp.NEGATE -> -v
                    ProgUnaryOp.NOT -> v.inv()
                }
            }
            is ProgExpr.Binary -> binary(expr.op, eval(expr.left, context), eval(expr.right, context), context)
        }
        return context.wordSize.normalize(raw, context.signed)
    }

    private fun binary(op: ProgBinaryOp, a: Long, b: Long, context: ProgContext): Long {
        val size = context.wordSize
        val signed = context.signed
        return when (op) {
            ProgBinaryOp.ADD -> a + b
            ProgBinaryOp.SUBTRACT -> a - b
            ProgBinaryOp.MULTIPLY -> a * b
            ProgBinaryOp.DIVIDE -> {
                if (b == 0L) throw ProgException(ProgError.DIVISION_BY_ZERO)
                // 符号ありは 0 に向かって切り捨て。最小値 ÷ −1 は Long の割り算でも例外にならず回り込む
                if (signed) a / b else java.lang.Long.divideUnsigned(size.pattern(a), size.pattern(b))
            }
            ProgBinaryOp.MOD -> {
                if (b == 0L) throw ProgException(ProgError.DIVISION_BY_ZERO)
                if (signed) a % b else java.lang.Long.remainderUnsigned(size.pattern(a), size.pattern(b))
            }
            ProgBinaryOp.AND -> a and b
            ProgBinaryOp.OR -> a or b
            ProgBinaryOp.XOR -> a xor b
            ProgBinaryOp.NAND -> (a and b).inv()
            ProgBinaryOp.NOR -> (a or b).inv()
            ProgBinaryOp.SHL -> shiftCount(b, context)?.let { a shl it } ?: 0L
            ProgBinaryOp.SHR -> {
                val n = shiftCount(b, context)
                when {
                    // 符号ありは算術シフト（空いたビットを符号で埋める）
                    signed -> if (n == null) (if (a < 0) -1L else 0L) else a shr n
                    // 符号なしは論理シフト（0 で埋める）
                    else -> if (n == null) 0L else size.pattern(a) ushr n
                }
            }
            ProgBinaryOp.ROL -> rotate(a, b, context, left = true)
            ProgBinaryOp.ROR -> rotate(a, b, context, left = false)
        }
    }

    /**
     * シフトの回数。語長以上なら null（全ビットが押し出される）。
     * 符号ありで負の回数はエラー。
     */
    private fun shiftCount(b: Long, context: ProgContext): Int? {
        if (context.signed && b < 0) throw ProgException(ProgError.NEGATIVE_SHIFT)
        val n = context.wordSize.pattern(b)
        return if (java.lang.Long.compareUnsigned(n, context.wordSize.bits.toLong()) >= 0) null else n.toInt()
    }

    /** 語長の中でビットを回す。回数は語長で割った余りを使う */
    private fun rotate(a: Long, b: Long, context: ProgContext, left: Boolean): Long {
        if (context.signed && b < 0) throw ProgException(ProgError.NEGATIVE_SHIFT)
        val size = context.wordSize
        val bits = size.bits
        val n = java.lang.Long.remainderUnsigned(size.pattern(b), bits.toLong()).toInt()
        if (n == 0) return a
        val pattern = size.pattern(a)
        val k = if (left) n else bits - n
        return ((pattern shl k) or (pattern ushr (bits - k))) and size.mask
    }
}
