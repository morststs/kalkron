package click.e17.kalkron.domain.scientific

/** 二項演算の種類 */
enum class BinaryOp { ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER }

/**
 * 構文木（式の木構造）。
 *
 * "2+3×4" は Binary(ADD, 2, Binary(MULTIPLY, 3, 4)) という形になり、
 * 木の形そのものが計算の順序を表す。評価は Evaluator が行う。
 */
sealed interface Expr {
    data class Num(val value: Double) : Expr

    /** 微分・積分の中で使う変数 x */
    data object Variable : Expr

    /** π・e・ANS・M */
    data class Constant(val symbol: Symbol) : Expr

    /** 単項マイナス（−3） */
    data class Negate(val operand: Expr) : Expr

    /** 後置の二乗（x²） */
    data class Square(val operand: Expr) : Expr

    data class Binary(val op: BinaryOp, val left: Expr, val right: Expr) : Expr

    data class Call(val function: MathFunction, val args: List<Expr>) : Expr
}
