package click.e17.kalkron.domain.scientific

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

/**
 * 評価器のテスト。文字列 → 字句解析 → 構文解析 → 評価 を通して結果を確かめる。
 */
class EvaluatorTest {

    private fun calc(source: String, context: EvalContext = EvalContext()): Double =
        Evaluator.evaluate(Parser.parse(Lexer.tokenize(source)), context)

    private val deg = EvalContext(angleUnit = AngleUnit.DEG)
    private val grad = EvalContext(angleUnit = AngleUnit.GRAD)

    @Test
    fun `優先順位どおりに計算する`() {
        assertEquals(14.0, calc("2+3×4"), 0.0)
        assertEquals(20.0, calc("(2+3)×4"), 0.0)
        assertEquals(512.0, calc("2^3^2"), 0.0)
        assertEquals(-4.0, calc("−2^2"), 0.0)
        assertEquals(0.5, calc("2^−1"), 0.0)
    }

    @Test
    fun `定数と暗黙の掛け算`() {
        assertEquals(2 * PI, calc("2π"), 1e-15)
        assertEquals(1.0, calc("ln(e)"), 1e-15)
    }

    @Test
    fun `ラジアンの三角関数`() {
        assertEquals(1.0, calc("sin(π÷2)"), 1e-15)
        assertEquals(PI, calc("cos⁻¹(−1)"), 1e-15)
    }

    @Test
    fun `ごく小さい結果は0にする`() {
        // Double では sin(π) = 1.22e-16 になるが、表示上は 0 にしたい
        assertEquals(0.0, calc("sin(π)"), 0.0)
        assertEquals(0.0, calc("1÷10^13"), 0.0)
    }

    @Test
    fun `度の三角関数`() {
        assertEquals(0.5, calc("sin(30)", deg), 1e-15)
        assertEquals(0.0, calc("cos(90)", deg), 0.0)
        assertEquals(1.0, calc("tan(45)", deg), 1e-15)
        assertEquals(90.0, calc("sin⁻¹(1)", deg), 1e-12)
    }

    @Test
    fun `グラードの三角関数`() {
        assertEquals(1.0, calc("sin(100)", grad), 1e-15)
    }

    @Test
    fun `直角のtanは定義域エラー`() {
        assertCalcError(CalcError.DOMAIN) { calc("tan(90)", deg) }
        assertCalcError(CalcError.DOMAIN) { calc("tan(−270)", deg) }
        assertCalcError(CalcError.DOMAIN) { calc("tan(100)", grad) }
    }

    @Test
    fun `双曲線関数とその逆関数`() {
        assertEquals(0.0, calc("sinh(0)"), 0.0)
        assertEquals(1.0, calc("cosh(0)"), 0.0)
        assertEquals(0.5493061443340548, calc("tanh⁻¹(0.5)"), 1e-15)
    }

    @Test
    fun `対数と平方根と指数`() {
        assertEquals(3.0, calc("log(1000)"), 1e-15)
        assertEquals(4.0, calc("√(16)"), 0.0)
        assertEquals(1.0, calc("exp(0)"), 0.0)
    }

    @Test
    fun `ANSとMの値を使う`() {
        assertEquals(5.0, calc("ANS+M", EvalContext(ans = 2.0, memory = 3.0)), 0.0)
    }

    @Test
    fun `微分と積分`() {
        assertEquals(1.0, calc("d/dx(sin(x),0)"), 1e-10)
        assertEquals(1.0 / 3, calc("∫(x²,0,1)"), 1e-12)
        // 2x を 0 から 1 まで積分すると 1（微分と積分の入れ子）
        assertEquals(1.0, calc("∫(d/dx(x²,x),0,1)"), 1e-9)
    }

    @Test
    fun `度のときの微分は度で測った傾きになる`() {
        assertEquals(PI / 180, calc("d/dx(sin(x),0)", deg), 1e-12)
    }

    @Test
    fun `エラーの種類`() {
        assertCalcError(CalcError.DIVISION_BY_ZERO) { calc("1÷0") }
        assertCalcError(CalcError.DIVISION_BY_ZERO) { calc("0÷0") }
        assertCalcError(CalcError.DOMAIN) { calc("ln(0)") }
        assertCalcError(CalcError.DOMAIN) { calc("ln(−1)") }
        assertCalcError(CalcError.DOMAIN) { calc("log(−1)") }
        assertCalcError(CalcError.DOMAIN) { calc("√(−4)") }
        assertCalcError(CalcError.DOMAIN) { calc("sin⁻¹(2)") }
        assertCalcError(CalcError.OVERFLOW) { calc("10^400") }
        assertCalcError(CalcError.INTEGRAL_NOT_CONVERGED) { calc("∫(1÷x,−1,2)") }
    }

    @Test
    fun `xは微分と積分の第1引数の中でだけ使える`() {
        assertCalcError(CalcError.VARIABLE_OUTSIDE_CALCULUS) { calc("x+1") }
        assertCalcError(CalcError.VARIABLE_OUTSIDE_CALCULUS) { calc("d/dx(x²,x)") }
    }

    @Test(timeout = 1_500)
    fun `入れ子の積分でも評価回数の上限を共有して短時間で終わる`() {
        // 内側の積分が外側の評価ごとに別の上限を持つと、20万×20万回になり固まっていた
        try {
            calc("∫(∫(sin(x),0,x),0,100)")
        } catch (e: CalcException) {
            assertEquals(CalcError.INTEGRAL_NOT_CONVERGED, e.error)
        }
    }

    @Test
    fun `度のcos90で割るとゼロ除算になる`() {
        // cos(90°) は Double では 6.1e-17 になり、1÷cos(90) が 1.6E+16 という誤答になっていた
        assertCalcError(CalcError.DIVISION_BY_ZERO) { calc("1÷cos(90)", deg) }
        assertCalcError(CalcError.DIVISION_BY_ZERO) { calc("1÷sin(π)") }
    }
}
