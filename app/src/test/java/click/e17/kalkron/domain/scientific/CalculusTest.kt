package click.e17.kalkron.domain.scientific

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.cos
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 数値微分・数値積分のテスト。答えが分かっている関数で精度を確かめる。
 */
class CalculusTest {

    @Test
    fun `sinの0での傾きは1`() {
        assertEquals(1.0, Calculus.derivative({ sin(it) }, 0.0), 1e-10)
    }

    @Test
    fun `3次式の微分は誤差なく求まる`() {
        // 5点差分は4次式まで厳密なので、x³ の x=2 での傾き 12 は丸め誤差の範囲で一致する
        assertEquals(12.0, Calculus.derivative({ it * it * it }, 2.0), 1e-8)
    }

    @Test
    fun `指数関数の微分`() {
        assertEquals(E, Calculus.derivative({ exp(it) }, 1.0), 1e-9)
    }

    @Test
    fun `大きな点でも相対的に正確`() {
        assertEquals(2000.0, Calculus.derivative({ it * it }, 1000.0), 1e-6)
    }

    @Test
    fun `x二乗の0から1までの積分は3分の1`() {
        assertEquals(1.0 / 3, Calculus.integrate({ it * it }, 0.0, 1.0), 1e-12)
    }

    @Test
    fun `sinの0からπまでの積分は2`() {
        assertEquals(2.0, Calculus.integrate({ sin(it) }, 0.0, PI), 1e-10)
    }

    @Test
    fun `区間が逆順なら符号が反転する`() {
        assertEquals(-1.0 / 3, Calculus.integrate({ it * it }, 1.0, 0.0), 1e-12)
    }

    @Test
    fun `区間の幅が0なら0`() {
        assertEquals(0.0, Calculus.integrate({ it * it }, 2.0, 2.0), 0.0)
    }

    @Test
    fun `値が大きい積分も収束する`() {
        assertEquals(1e9 / 3, Calculus.integrate({ it * it }, 0.0, 1000.0), 1e-3)
    }

    @Test
    fun `発散する積分は収束しないエラー`() {
        // 1/x は 0 の近くで発散する。分割点はちょうど 0 にならないので割り算は起きない
        assertCalcError(CalcError.INTEGRAL_NOT_CONVERGED) {
            Calculus.integrate({ 1.0 / it }, -1.0, 2.0)
        }
    }

    @Test
    fun `関数が定義されない区間は定義域エラー`() {
        assertCalcError(CalcError.DOMAIN) {
            Calculus.integrate({ sqrt(it) }, -2.0, -1.0)
        }
    }

    @Test(timeout = 5_000)
    fun `激しく振動する関数でも固まらない`() {
        // 値が出ても収束エラーでもよいが、評価回数の上限で必ず短時間に終わること
        try {
            Calculus.integrate({ sin(1.0 / it) }, 1e-6, 1.0)
        } catch (e: CalcException) {
            assertEquals(CalcError.INTEGRAL_NOT_CONVERGED, e.error)
        }
    }

    @Test
    fun `端点で傾きが発散する関数も積分できる`() {
        // √x は x=0 で傾きが無限大になる。細かく分けても許容誤差に届かず失敗していた
        assertEquals(2.0 / 3, Calculus.integrate({ sqrt(it) }, 0.0, 1.0), 1e-10)
        // 半径1の半円の面積
        assertEquals(PI / 2, Calculus.integrate({ sqrt(1 - it * it) }, -1.0, 1.0), 1e-9)
    }

    @Test
    fun `周期関数の積分で標本点の偶然の一致に騙されない`() {
        // 両端・中点・1/4点の値がたまたま揃うと、1回目の判定で誤った値を受け入れていた
        assertEquals(2 * PI, Calculus.integrate({ sin(it) * sin(it) }, 0.0, 4 * PI), 1e-9)
        assertEquals(2 * PI, Calculus.integrate({ cos(it) * cos(it) }, 0.0, 4 * PI), 1e-9)
        assertEquals(2.0, Calculus.integrate({ sin(PI * it) * sin(PI * it) }, 0.0, 4.0), 1e-9)
    }

    @Test
    fun `評価回数の上限に達したら収束しないエラー`() {
        assertCalcError(CalcError.INTEGRAL_NOT_CONVERGED) {
            Calculus.integrate({ sin(it) }, 0.0, 1e6, EvaluationBudget(limit = 100))
        }
    }
}
