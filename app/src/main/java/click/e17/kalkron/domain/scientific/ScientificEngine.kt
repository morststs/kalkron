package click.e17.kalkron.domain.scientific

import click.e17.kalkron.domain.Calculation
import click.e17.kalkron.domain.CalculatorMode

/**
 * 関数電卓の内部状態。
 *
 * @param tokens 入力中の式
 * @param second 2nd が ON か（次の関数キー1つにだけ効く）
 * @param hyperbolic HYP が ON か（次の関数キー1つにだけ効く）
 * @param ans 直前に確定した結果
 * @param memory STO で保存した値。未保存なら null（式の中では 0 として扱う）
 * @param result = で確定した結果。確定直後だけ値が入る
 * @param error = で起きたエラー。エラー表示中だけ値が入る
 */
data class ScientificState(
    val tokens: List<Token> = emptyList(),
    val angleUnit: AngleUnit = AngleUnit.RAD,
    val second: Boolean = false,
    val hyperbolic: Boolean = false,
    val ans: Double = 0.0,
    val memory: Double? = null,
    val result: Double? = null,
    val error: CalcError? = null,
)

/** 操作を適用した結果。completed は = で確定した計算（履歴に保存する） */
data class ScientificResult(
    val state: ScientificState,
    val completed: Calculation? = null,
)

/**
 * 関数電卓の入力の編集と評価。
 * STD の CalculatorEngine と同じく「状態 + 操作 → 新しい状態」の純粋関数として書く。
 */
object ScientificEngine {

    private val CALCULUS = setOf(MathFunction.DERIVATIVE, MathFunction.INTEGRAL)

    /** = の直後に押すと、ANS の続きとして入力する記号 */
    private val CONTINUATIONS = setOf(
        Symbol.PLUS, Symbol.MINUS, Symbol.TIMES, Symbol.DIVIDE, Symbol.POWER, Symbol.SQUARE,
    )

    fun reduce(state: ScientificState, action: ScientificAction): ScientificResult = when (action) {
        is ScientificAction.Digit -> ScientificResult(appendDigit(state, action.value))
        ScientificAction.Decimal -> ScientificResult(appendDecimal(state))
        is ScientificAction.Insert -> ScientificResult(append(state, Token.Sym(action.symbol)))
        is ScientificAction.InsertFunction -> ScientificResult(append(state, Token.Fn(action.function)))
        is ScientificAction.Key -> ScientificResult(
            append(state, resolve(action.key, state.second, state.hyperbolic)).resetModifiers()
        )
        ScientificAction.Store -> ScientificResult(store(state))
        ScientificAction.ToggleSecond -> ScientificResult(state.copy(second = !state.second))
        ScientificAction.ToggleHyperbolic -> ScientificResult(state.copy(hyperbolic = !state.hyperbolic))
        // 確定結果は前の単位で計算した値なので消し、新しい単位で先出しし直す
        is ScientificAction.SetAngleUnit -> ScientificResult(state.copy(angleUnit = action.unit, result = null))
        ScientificAction.Delete -> ScientificResult(delete(state))
        ScientificAction.Clear -> ScientificResult(
            state.copy(tokens = emptyList(), result = null, error = null).resetModifiers()
        )
        ScientificAction.Equals -> evaluate(state)
    }

    /** キーと 2nd / HYP の状態から、実際に入れるトークンを決める */
    fun resolve(key: SciKey, second: Boolean, hyperbolic: Boolean): Token = when (key) {
        SciKey.SIN -> Token.Fn(trig(MathFunction.SIN, MathFunction.ASIN, MathFunction.SINH, MathFunction.ASINH, second, hyperbolic))
        SciKey.COS -> Token.Fn(trig(MathFunction.COS, MathFunction.ACOS, MathFunction.COSH, MathFunction.ACOSH, second, hyperbolic))
        SciKey.TAN -> Token.Fn(trig(MathFunction.TAN, MathFunction.ATAN, MathFunction.TANH, MathFunction.ATANH, second, hyperbolic))
        SciKey.LN -> Token.Fn(if (second) MathFunction.LOG else MathFunction.LN)
        SciKey.X_SQUARED -> Token.Sym(if (second) Symbol.POWER else Symbol.SQUARE)
        SciKey.SQRT -> Token.Fn(if (second) MathFunction.EXP else MathFunction.SQRT)
    }

    /**
     * 入力途中の式を評価してみる。式として成り立たない・エラーになるときは null。
     * 先出しはキー操作のたびにメインスレッドで計算するので、重くなりうる微分・積分を
     * 含む式は先出しせず、= のときだけ計算する。
     */
    fun preview(state: ScientificState): Double? {
        if (state.tokens.isEmpty() || state.result != null || state.error != null) return null
        if (state.tokens.any { it is Token.Fn && it.function in CALCULUS }) return null
        return try {
            Evaluator.evaluate(Parser.parse(state.tokens), state.context())
        } catch (e: CalcException) {
            null
        }
    }

    private fun trig(
        normal: MathFunction,
        inverse: MathFunction,
        hyperbolic: MathFunction,
        inverseHyperbolic: MathFunction,
        second: Boolean,
        hyp: Boolean,
    ): MathFunction = when {
        second && hyp -> inverseHyperbolic
        hyp -> hyperbolic
        second -> inverse
        else -> normal
    }

    /**
     * 入力の前処理。
     * = の直後なら、演算子は ANS の続きに、それ以外は新しい式にする。エラー表示中ならエラーを消す。
     */
    private fun prepare(state: ScientificState, next: Token): ScientificState = when {
        state.result != null -> {
            val continues = next is Token.Sym && next.symbol in CONTINUATIONS
            state.copy(
                tokens = if (continues) listOf(Token.Sym(Symbol.ANS)) else emptyList(),
                result = null,
            )
        }
        state.error != null -> state.copy(error = null)
        else -> state
    }

    private fun append(state: ScientificState, token: Token): ScientificState {
        val prepared = prepare(state, token)
        return prepared.copy(tokens = prepared.tokens + token)
    }

    private fun appendDigit(state: ScientificState, digit: Int): ScientificState {
        val prepared = prepare(state, Token.Num(digit.toString()))
        val last = prepared.tokens.lastOrNull()
        return if (last is Token.Num) {
            val digits = if (last.digits == "0") digit.toString() else last.digits + digit
            prepared.copy(tokens = prepared.tokens.dropLast(1) + Token.Num(digits))
        } else {
            prepared.copy(tokens = prepared.tokens + Token.Num(digit.toString()))
        }
    }

    private fun appendDecimal(state: ScientificState): ScientificState {
        val prepared = prepare(state, Token.Num("0."))
        val last = prepared.tokens.lastOrNull()
        return when {
            last is Token.Num && '.' in last.digits -> prepared
            last is Token.Num -> prepared.copy(tokens = prepared.tokens.dropLast(1) + Token.Num(last.digits + "."))
            else -> prepared.copy(tokens = prepared.tokens + Token.Num("0."))
        }
    }

    /** STO: 直前の結果を M に保存する。2nd のときは RCL として M を式に入れる */
    private fun store(state: ScientificState): ScientificState =
        if (state.second) {
            append(state, Token.Sym(Symbol.MEMORY)).resetModifiers()
        } else {
            state.copy(memory = state.ans).resetModifiers()
        }

    /** ⌫: 数値は1文字ずつ、それ以外のトークンはまとめて消す */
    private fun delete(state: ScientificState): ScientificState {
        val cleared = state.copy(result = null, error = null)
        val last = cleared.tokens.lastOrNull() ?: return cleared
        return if (last is Token.Num && last.digits.length > 1) {
            cleared.copy(tokens = cleared.tokens.dropLast(1) + Token.Num(last.digits.dropLast(1)))
        } else {
            cleared.copy(tokens = cleared.tokens.dropLast(1))
        }
    }

    private fun evaluate(state: ScientificState): ScientificResult {
        // 空の式、または確定済み（= の連打）なら何もしない
        if (state.tokens.isEmpty() || state.result != null) return ScientificResult(state)
        return try {
            val value = Evaluator.evaluate(Parser.parse(state.tokens), state.context())
            val calculation = Calculation(
                // 履歴には ANS・M を値に置き換えた式を残す
                expression = ScientificFormat.expression(
                    state.tokens,
                    values = mapOf(Symbol.ANS to state.ans, Symbol.MEMORY to (state.memory ?: 0.0)),
                ),
                result = ScientificFormat.number(value),
                mode = CalculatorMode.SCIENTIFIC,
            )
            ScientificResult(state.copy(result = value, ans = value, error = null), completed = calculation)
        } catch (e: CalcException) {
            ScientificResult(state.copy(error = e.error))
        }
    }

    private fun ScientificState.context() = EvalContext(angleUnit, ans, memory ?: 0.0)

    private fun ScientificState.resetModifiers() = copy(second = false, hyperbolic = false)
}
