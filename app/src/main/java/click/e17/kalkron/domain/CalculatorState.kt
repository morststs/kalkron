package click.e17.kalkron.domain

import java.math.BigDecimal

/**
 * 電卓の内部状態。
 *
 * Android の API に一切依存しないただのデータクラスなので、
 * JVM 上のユニットテストでそのまま扱える。
 *
 * @param input 現在入力中（または結果表示中）の数値の文字列表現
 * @param accumulator 演算子キーを押したときに保持される左辺の値
 * @param pendingOperator 入力待ちの演算子。null なら演算子は押されていない
 * @param isError 0 除算などでエラー状態になっているか
 * @param startsNewInput 次に数字キーが押されたとき input を置き換えるか
 *                       （= や演算子キーの直後は true になる）
 */
data class CalculatorState(
    val input: String = "0",
    val accumulator: BigDecimal? = null,
    val pendingOperator: Operator? = null,
    val isError: Boolean = false,
    val startsNewInput: Boolean = true,
) {
    /**
     * 画面上部に出す途中式のプレビュー（例: "12 +"）。
     * 状態から計算できる値なので、別のフィールドとして持たず computed property にしている。
     */
    val expression: String
        get() = if (accumulator != null && pendingOperator != null) {
            "${CalculatorEngine.format(accumulator)} ${pendingOperator.symbol}"
        } else {
            ""
        }
}

/**
 * 「= まで到達して完了した 1 回の計算」。履歴として保存する単位。
 *
 * @param mode どのモードの計算か。標準モードは省略できるよう既定値を持つ
 */
data class Calculation(
    val expression: String,
    val result: String,
    val mode: CalculatorMode = CalculatorMode.STANDARD,
)

/**
 * [CalculatorEngine.reduce] の戻り値。
 *
 * 新しい状態に加えて「この操作で計算が 1 つ完了したか」を返すことで、
 * ViewModel 側は completed != null のときだけ履歴を保存すればよくなる。
 */
data class EngineResult(
    val state: CalculatorState,
    val completed: Calculation? = null,
)
