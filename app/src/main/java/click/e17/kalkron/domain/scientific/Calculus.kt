package click.e17.kalkron.domain.scientific

import kotlin.math.abs
import kotlin.math.max

/**
 * 数値微分と数値積分。どちらも関数 f を何度も呼んで近似値を求める。
 */
object Calculus {

    /** 積分で区間を半分に分ける回数の上限 */
    private const val MAX_DEPTH = 50

    /** 積分の許容誤差（結果の大きさに対する割合）。表示の12桁に合わせている */
    private const val RELATIVE_TOLERANCE = 1e-12

    /**
     * 区間ごとの許容誤差の下限（結果の大きさに対する割合）。
     * 許容誤差は分割のたびに半分にするが、下限が無いと √x のように端で傾きが
     * 発散する関数では、深さの上限までに誤差が許容誤差を下回れず失敗する。
     */
    private const val TOLERANCE_FLOOR = 1e-15

    /**
     * 最初に無条件で分割する段数。周期関数では、両端・中点・1/4点の値が
     * たまたま揃って「誤差 0」に見え、1回目の判定で誤った値を受け入れてしまうため。
     */
    private const val MIN_DEPTH = 4

    /**
     * 丸め誤差とみなす割合。微分・積分の結果が「関数の値の大きさから見込まれる大きさ」に対して
     * この割合より小さければ 0 にする（∫(sin(x), 0, 2π) が 1.1e-16 になるのを防ぐ）。
     * 関数の値そのものと比べるので、∫(x^20, 0, 0.1) のような本当に小さい値は残る。
     */
    private const val NOISE_RATIO = 1e-12

    /**
     * f の a における傾き（微分係数）を5点差分公式で求める。
     * f'(a) ≈ [−f(a+2h) + 8f(a+h) − 8f(a−h) + f(a−2h)] / 12h
     */
    fun derivative(f: (Double) -> Double, a: Double): Double {
        val h = 1e-3 * max(1.0, abs(a))
        val values = listOf(f(a + 2 * h), f(a + h), f(a - h), f(a - 2 * h))
        val slope = (-values[0] + 8 * values[1] - 8 * values[2] + values[3]) / (12 * h)
        // 関数の値の丸め誤差は h で割られて大きくなるため、「関数の値 ÷ h」を基準に判定する
        val scale = values.maxOf { abs(it) } / h
        return if (abs(slope) < NOISE_RATIO * scale) 0.0 else slope
    }

    /**
     * f の a から b までの定積分を適応シンプソン法で求める。
     * 誤差の大きい区間だけを細かく分けていくので、滑らかな部分は少ない計算で済む。
     */
    fun integrate(
        f: (Double) -> Double,
        a: Double,
        b: Double,
        budget: EvaluationBudget = EvaluationBudget(),
    ): Double {
        if (a == b) return 0.0
        val counted = CountedFunction(f, budget)
        val fa = counted(a)
        val fb = counted(b)
        val fm = counted((a + b) / 2)
        val whole = simpson(a, b, fa, fm, fb)
        val scale = max(1.0, abs(whole))
        val tolerance = RELATIVE_TOLERANCE * scale
        val minTolerance = TOLERANCE_FLOOR * scale
        val area = refine(counted, a, b, fa, fm, fb, whole, tolerance, minTolerance, MAX_DEPTH)
        // 「区間の幅 × 関数の値の最大」が、打ち消し合いが無いときに見込まれる積分の大きさ
        val expectedSize = abs(b - a) * counted.maxAbs
        return if (abs(area) < NOISE_RATIO * expectedSize) 0.0 else area
    }

    /** シンプソン則: 区間 [a, b] の積分を、両端と中点の値の重み付き平均で近似する */
    private fun simpson(a: Double, b: Double, fa: Double, fm: Double, fb: Double): Double =
        (b - a) / 6 * (fa + 4 * fm + fb)

    private fun refine(
        f: CountedFunction,
        a: Double,
        b: Double,
        fa: Double,
        fm: Double,
        fb: Double,
        whole: Double,
        tolerance: Double,
        minTolerance: Double,
        depth: Int,
    ): Double {
        val m = (a + b) / 2
        val flm = f((a + m) / 2)
        val frm = f((m + b) / 2)
        val left = simpson(a, m, fa, flm, fm)
        val right = simpson(m, b, fm, frm, fb)
        val delta = left + right - whole

        // 最初の数段は無条件に分ける。それ以降、半分に分けても値がほとんど
        // 変わらなければ、その区間は十分に正確とみなす
        val forced = MAX_DEPTH - depth < MIN_DEPTH
        if (!forced && abs(delta) <= 15 * tolerance) return left + right + delta / 15
        if (depth <= 0) throw CalcException(CalcError.INTEGRAL_NOT_CONVERGED)

        val half = max(tolerance / 2, minTolerance)
        return refine(f, a, m, fa, flm, fm, left, half, minTolerance, depth - 1) +
            refine(f, m, b, fm, frm, fb, right, half, minTolerance, depth - 1)
    }

    /** f を呼ぶたびに予算を1つ使う。NaN は定義域エラーにする。値の絶対値の最大も記録する */
    private class CountedFunction(
        private val f: (Double) -> Double,
        private val budget: EvaluationBudget,
    ) {
        var maxAbs = 0.0
            private set

        operator fun invoke(t: Double): Double {
            budget.spend()
            val y = f(t)
            if (y.isNaN()) throw CalcException(CalcError.DOMAIN)
            maxAbs = max(maxAbs, abs(y))
            return y
        }
    }
}

/**
 * 積分で関数を呼んでよい回数の予算。振動の激しい関数で処理が終わらなくなるのを防ぐ。
 *
 * 1回の計算（= を1回押す）全体で1つを共有する。積分を入れ子にしたとき、
 * 内側の積分ごとに新しい予算を持たせると、呼び出し回数が掛け算で増えて固まるため。
 */
class EvaluationBudget(private val limit: Int = 200_000) {
    private var used = 0

    fun spend() {
        used++
        if (used > limit) throw CalcException(CalcError.INTEGRAL_NOT_CONVERGED)
    }
}
