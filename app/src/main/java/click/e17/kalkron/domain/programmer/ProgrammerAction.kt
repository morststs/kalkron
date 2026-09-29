package click.e17.kalkron.domain.programmer

/**
 * プログラマーモードに対するユーザー操作。UI はこれを ViewModel に渡すだけ。
 */
sealed interface ProgrammerAction {
    /** 数字キー（0〜15。A〜F は 10〜15） */
    data class Digit(val value: Int) : ProgrammerAction

    /** 演算子・括弧・ANS のキー */
    data class Insert(val symbol: ProgSymbol) : ProgrammerAction

    /** 2's（いま編集している数の符号反転） */
    data object Complement : ProgrammerAction

    /** ビット反転グリッドのマス */
    data class FlipBit(val index: Int) : ProgrammerAction

    data class SetRadix(val radix: Radix) : ProgrammerAction

    data class SetWordSize(val wordSize: WordSize) : ProgrammerAction

    data object ToggleSigned : ProgrammerAction

    /** ⌫ */
    data object Delete : ProgrammerAction

    /** AC */
    data object Clear : ProgrammerAction

    /** = */
    data object Equals : ProgrammerAction
}
