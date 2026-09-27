package click.e17.kalkron.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Locale

/**
 * 電卓の計算ロジック本体。
 *
 * 「現在の状態 + 操作 → 新しい状態」を返すだけの純粋関数の集まりで、
 * 内部に可変な状態を持たない（object だが state を保持していない点に注意）。
 * こうしておくと
 *   - 同じ入力なら必ず同じ結果になるのでテストが書きやすい
 *   - Android に依存しないので端末なしでテストできる
 * という利点がある。MVVM でいう「ドメイン層」にあたる部分。
 */
object CalculatorEngine {

    /** 入力できる桁数の上限 */
    const val MAX_INPUT_DIGITS = 12

    /** エラー時に表示する文字列 */
    const val ERROR_TEXT = "エラー"

    /** 有効桁数 12 桁で丸める。割り算の結果が無限小数になる場合に必要 */
    private val MATH_CONTEXT = MathContext(12, RoundingMode.HALF_UP)

    /** これより長くなる結果は指数表記に切り替える */
    private const val MAX_PLAIN_LENGTH = 16

    /**
     * 操作を 1 つ適用して次の状態を返す。
     */
    fun reduce(state: CalculatorState, action: CalculatorAction): EngineResult = when (action) {
        is CalculatorAction.Digit -> EngineResult(appendDigit(state, action.value))
        is CalculatorAction.Decimal -> EngineResult(appendDecimal(state))
        is CalculatorAction.Operate -> applyOperator(state, action.operator)
        is CalculatorAction.Equals -> calculate(state)
        is CalculatorAction.Clear -> EngineResult(CalculatorState())
        is CalculatorAction.Delete -> EngineResult(deleteLast(state))
        is CalculatorAction.ToggleSign -> EngineResult(toggleSign(state))
        is CalculatorAction.Percent -> EngineResult(percent(state))
    }

    // ------------------------------------------------------------------
    // 数値の入力
    // ------------------------------------------------------------------

    private fun appendDigit(state: CalculatorState, digit: Int): CalculatorState {
        // エラー中に数字が押されたら、リセットしてから入力を受け付ける
        val base = if (state.isError) CalculatorState() else state
        val current = if (base.startsNewInput) "0" else base.input

        val next = when (current) {
            "0" -> digit.toString()
            "-0" -> "-$digit"
            else -> current + digit
        }
        // 桁数上限を超える入力は無視する
        if (next.count { it.isDigit() } > MAX_INPUT_DIGITS) return base

        return base.copy(input = next, startsNewInput = false)
    }

    private fun appendDecimal(state: CalculatorState): CalculatorState {
        val base = if (state.isError) CalculatorState() else state
        return when {
            // 結果表示中に "." を押したら "0." から入力し直す
            base.startsNewInput -> base.copy(input = "0.", startsNewInput = false)
            // 小数点は 1 つまで
            base.input.contains('.') -> base
            else -> base.copy(input = base.input + ".")
        }
    }

    private fun toggleSign(state: CalculatorState): CalculatorState {
        if (state.isError) return state
        val next = when {
            state.input.startsWith("-") -> state.input.removePrefix("-")
            state.input == "0" -> state.input
            else -> "-${state.input}"
        }
        return state.copy(input = next, startsNewInput = false)
    }

    private fun percent(state: CalculatorState): CalculatorState {
        if (state.isError) return state
        val value = state.input.toBigDecimalOrZero()
        val divided = value.divide(BigDecimal(100), MATH_CONTEXT)
        return state.copy(input = format(divided), startsNewInput = false)
    }

    private fun deleteLast(state: CalculatorState): CalculatorState {
        if (state.isError) return CalculatorState()
        // 計算結果を表示している最中は 1 文字削除しない（誤操作防止）
        if (state.startsNewInput) return state

        val trimmed = state.input.dropLast(1)
        val next = if (trimmed.isEmpty() || trimmed == "-") "0" else trimmed
        return state.copy(input = next)
    }

    // ------------------------------------------------------------------
    // 演算
    // ------------------------------------------------------------------

    private fun applyOperator(state: CalculatorState, operator: Operator): EngineResult {
        val base = if (state.isError) CalculatorState() else state

        // 「2 + 3 +」のように演算子が連続した場合は、先に 2 + 3 を計算して結果を左辺にする
        if (base.pendingOperator != null && base.accumulator != null && !base.startsNewInput) {
            val evaluated = evaluate(base)
                ?: return EngineResult(errorState())
            return EngineResult(
                state = evaluated.state.copy(
                    accumulator = evaluated.state.input.toBigDecimalOrZero(),
                    pendingOperator = operator,
                ),
                completed = evaluated.completed,
            )
        }

        // それ以外は現在の入力値を左辺として保持し、演算子だけ差し替える
        return EngineResult(
            base.copy(
                accumulator = base.input.toBigDecimalOrZero(),
                pendingOperator = operator,
                startsNewInput = true,
            )
        )
    }

    private fun calculate(state: CalculatorState): EngineResult {
        if (state.isError) return EngineResult(CalculatorState())
        // 演算子が押されていなければ = は何もしない
        if (state.pendingOperator == null || state.accumulator == null) {
            return EngineResult(state.copy(startsNewInput = true))
        }
        return evaluate(state) ?: EngineResult(errorState())
    }

    /**
     * 保留中の演算を実行する。0 除算のときは null を返す。
     */
    private fun evaluate(state: CalculatorState): EngineResult? {
        val left = state.accumulator ?: return null
        val operator = state.pendingOperator ?: return null
        val right = state.input.toBigDecimalOrZero()

        val result = when (operator) {
            Operator.ADD -> left.add(right)
            Operator.SUBTRACT -> left.subtract(right)
            Operator.MULTIPLY -> left.multiply(right)
            Operator.DIVIDE -> if (right.signum() == 0) return null else left.divide(right, MATH_CONTEXT)
        }

        val formatted = format(result)
        return EngineResult(
            state = CalculatorState(input = formatted, startsNewInput = true),
            completed = Calculation(
                expression = "${format(left)} ${operator.symbol} ${format(right)}",
                result = formatted,
            ),
        )
    }

    private fun errorState() = CalculatorState(input = ERROR_TEXT, isError = true)

    // ------------------------------------------------------------------
    // 表示用の整形
    // ------------------------------------------------------------------

    /**
     * BigDecimal を画面表示用の文字列に変換する。
     * 末尾の余分な 0 を落とし、桁数が多すぎる場合は指数表記にする。
     */
    fun format(value: BigDecimal): String {
        val rounded = value.round(MATH_CONTEXT).stripTrailingZeros()
        // stripTrailingZeros は 100 を 1E+2 にしてしまうので、整数に戻してから文字列化する
        val normalized = if (rounded.scale() < 0) rounded.setScale(0) else rounded
        val plain = normalized.toPlainString()
        return if (plain.length > MAX_PLAIN_LENGTH) {
            String.format(Locale.US, "%.6E", normalized)
        } else {
            plain
        }
    }

    /**
     * 入力中の文字列を BigDecimal に変換する。
     * "0." や "-" のような「入力途中」の文字列でも落ちないようにしている。
     */
    private fun String.toBigDecimalOrZero(): BigDecimal {
        val sanitized = trimEnd('.')
        if (sanitized.isEmpty() || sanitized == "-") return BigDecimal.ZERO
        return sanitized.toBigDecimalOrNull() ?: BigDecimal.ZERO
    }
}
