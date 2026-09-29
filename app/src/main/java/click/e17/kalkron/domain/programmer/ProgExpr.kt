package click.e17.kalkron.domain.programmer

/** 二項演算の種類 */
enum class ProgBinaryOp { ADD, SUBTRACT, MULTIPLY, DIVIDE, MOD, AND, NAND, OR, NOR, XOR, SHL, SHR, ROL, ROR }

/** 単項演算の種類 */
enum class ProgUnaryOp { NEGATE, NOT }

/**
 * 構文木。木の形そのものが計算の順序を表す。評価は ProgEvaluator が行う。
 */
sealed interface ProgExpr {
    data class Num(val value: Long) : ProgExpr

    /** 直前の結果 */
    data object Ans : ProgExpr

    data class Unary(val op: ProgUnaryOp, val operand: ProgExpr) : ProgExpr

    data class Binary(val op: ProgBinaryOp, val left: ProgExpr, val right: ProgExpr) : ProgExpr
}
