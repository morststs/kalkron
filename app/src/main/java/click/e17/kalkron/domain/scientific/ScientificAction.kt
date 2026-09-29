package click.e17.kalkron.domain.scientific

/** 2nd / HYP の状態によって入るものが変わるキー */
enum class SciKey { SIN, COS, TAN, LN, X_SQUARED, SQRT }

/**
 * 関数電卓に対するユーザー操作。UI はこれを ViewModel に渡すだけ。
 */
sealed interface ScientificAction {
    /** 数字キー（0〜9） */
    data class Digit(val value: Int) : ScientificAction

    /** 小数点キー */
    data object Decimal : ScientificAction

    /** 記号をそのまま入れるキー（+ − × ÷ ( ) , x π e ANS） */
    data class Insert(val symbol: Symbol) : ScientificAction

    /** 2nd / HYP の影響を受けない関数キー（d/dx、∫dx） */
    data class InsertFunction(val function: MathFunction) : ScientificAction

    /** 2nd / HYP の影響を受けるキー */
    data class Key(val key: SciKey) : ScientificAction

    /** STO（2nd のときは RCL） */
    data object Store : ScientificAction

    data object ToggleSecond : ScientificAction

    data object ToggleHyperbolic : ScientificAction

    data class SetAngleUnit(val unit: AngleUnit) : ScientificAction

    /** ⌫ */
    data object Delete : ScientificAction

    /** AC */
    data object Clear : ScientificAction

    /** = */
    data object Equals : ScientificAction
}
