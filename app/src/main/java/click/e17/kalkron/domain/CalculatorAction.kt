package click.e17.kalkron.domain

/**
 * 電卓に対するユーザー操作を表す型。
 *
 * UI 層はこの型の値を ViewModel に渡すだけで、計算の中身を一切知らなくてよい。
 * sealed interface にしておくと when 式で分岐漏れをコンパイラが検出してくれる。
 */
sealed interface CalculatorAction {
    /** 数字キー（0〜9） */
    data class Digit(val value: Int) : CalculatorAction

    /** 小数点キー */
    data object Decimal : CalculatorAction

    /** 四則演算キー */
    data class Operate(val operator: Operator) : CalculatorAction

    /** = キー */
    data object Equals : CalculatorAction

    /** AC キー（全消去） */
    data object Clear : CalculatorAction

    /** DEL キー（1 文字削除） */
    data object Delete : CalculatorAction

    /** +/- キー（符号反転） */
    data object ToggleSign : CalculatorAction

    /** % キー（100 で割る） */
    data object Percent : CalculatorAction
}

/** 四則演算の種類。symbol は画面表示に使う記号。 */
enum class Operator(val symbol: String) {
    ADD("+"),
    SUBTRACT("−"),
    MULTIPLY("×"),
    DIVIDE("÷"),
}
