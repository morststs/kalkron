package click.e17.kalkron.domain.scientific

import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.acosh
import kotlin.math.asin
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.atanh
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

/**
 * 評価に必要な外部の値。
 * 角度単位・直前の結果（ANS）・メモリ（M）は式の外で決まるので、ここにまとめて渡す。
 */
data class EvalContext(
    val angleUnit: AngleUnit = AngleUnit.RAD,
    val ans: Double = 0.0,
    val memory: Double = 0.0,
)

/**
 * 評価器: 構文木をたどって値を計算する。
 */
object Evaluator {

    /** これより絶対値が小さい結果は 0 として扱う（sin(π) を 0 にするため） */
    private const val ZERO_THRESHOLD = 1e-12

    fun evaluate(expr: Expr, context: EvalContext = EvalContext()): Double {
        // 積分の評価回数の予算は、入れ子の積分も含めて1回の評価全体で共有する
        val value = eval(expr, context, x = null, budget = EvaluationBudget())
        return when {
            value.isNaN() -> throw CalcException(CalcError.DOMAIN)
            value.isInfinite() -> throw CalcException(CalcError.OVERFLOW)
            abs(value) < ZERO_THRESHOLD -> 0.0
            else -> value
        }
    }

    /**
     * @param x 変数 x の値。微分・積分の第1引数を評価するときだけ値が入り、それ以外は null
     */
    private fun eval(expr: Expr, context: EvalContext, x: Double?, budget: EvaluationBudget): Double =
        when (expr) {
            is Expr.Num -> expr.value
            Expr.Variable -> x ?: throw CalcException(CalcError.VARIABLE_OUTSIDE_CALCULUS)
            is Expr.Constant -> constant(expr.symbol, context)
            is Expr.Negate -> -eval(expr.operand, context, x, budget)
            is Expr.Square -> eval(expr.operand, context, x, budget).let { it * it }
            is Expr.Binary -> binary(expr, context, x, budget)
            is Expr.Call -> call(expr, context, x, budget)
        }

    private fun snapToZero(value: Double): Double = if (abs(value) < ZERO_THRESHOLD) 0.0 else value

    private fun constant(symbol: Symbol, context: EvalContext): Double = when (symbol) {
        Symbol.PI -> PI
        Symbol.E -> E
        Symbol.ANS -> context.ans
        Symbol.MEMORY -> context.memory
        else -> throw CalcException(CalcError.SYNTAX)
    }

    private fun binary(expr: Expr.Binary, context: EvalContext, x: Double?, budget: EvaluationBudget): Double {
        val left = eval(expr.left, context, x, budget)
        val right = eval(expr.right, context, x, budget)
        return when (expr.op) {
            BinaryOp.ADD -> left + right
            BinaryOp.SUBTRACT -> left - right
            BinaryOp.MULTIPLY -> left * right
            BinaryOp.DIVIDE ->
                if (right == 0.0) throw CalcException(CalcError.DIVISION_BY_ZERO) else left / right
            BinaryOp.POWER -> left.pow(right)
        }
    }

    private fun call(expr: Expr.Call, context: EvalContext, x: Double?, budget: EvaluationBudget): Double {
        val unit = context.angleUnit
        return when (expr.function) {
            // 微分・積分: 第1引数を「x を受け取って値を返す関数」として Calculus に渡す
            MathFunction.DERIVATIVE -> {
                val point = eval(expr.args[1], context, x, budget)
                Calculus.derivative({ t -> eval(expr.args[0], context, t, budget) }, point)
            }

            MathFunction.INTEGRAL -> {
                val lower = eval(expr.args[1], context, x, budget)
                val upper = eval(expr.args[2], context, x, budget)
                Calculus.integrate({ t -> eval(expr.args[0], context, t, budget) }, lower, upper, budget)
            }

            else -> {
                val v = eval(expr.args[0], context, x, budget)
                when (expr.function) {
                    // 三角関数の出力は、その場で微小値を 0 にする。
                    // cos(90°) が 6.1e-17 のまま割り算に使われると 1÷cos(90) が巨大な誤答になるため
                    MathFunction.SIN -> snapToZero(sin(unit.toRadians(v)))
                    MathFunction.COS -> snapToZero(cos(unit.toRadians(v)))
                    MathFunction.TAN -> {
                        if (unit.isOddRightAngle(v)) throw CalcException(CalcError.DOMAIN)
                        snapToZero(tan(unit.toRadians(v)))
                    }
                    MathFunction.ASIN -> unit.fromRadians(asin(v))
                    MathFunction.ACOS -> unit.fromRadians(acos(v))
                    MathFunction.ATAN -> unit.fromRadians(atan(v))
                    MathFunction.SINH -> sinh(v)
                    MathFunction.COSH -> cosh(v)
                    MathFunction.TANH -> tanh(v)
                    MathFunction.ASINH -> asinh(v)
                    MathFunction.ACOSH -> acosh(v)
                    MathFunction.ATANH -> atanh(v)
                    MathFunction.LN ->
                        if (v <= 0.0) throw CalcException(CalcError.DOMAIN) else ln(v)
                    MathFunction.LOG ->
                        if (v <= 0.0) throw CalcException(CalcError.DOMAIN) else log10(v)
                    // 負の数の平方根は NaN になり、evaluate で定義域エラーになる
                    MathFunction.SQRT -> sqrt(v)
                    MathFunction.EXP -> exp(v)
                    // 上の分岐で処理済み
                    MathFunction.DERIVATIVE, MathFunction.INTEGRAL ->
                        throw CalcException(CalcError.SYNTAX)
                }
            }
        }
    }
}
