package click.e17.kalkron.domain.programmer

/**
 * 記号のトークンの種類。text は画面に出す文字。
 */
enum class ProgSymbol(val text: String) {
    PLUS("+"),
    MINUS("−"),
    TIMES("×"),
    DIVIDE("÷"),
    MOD("MOD"),
    AND("AND"),
    OR("OR"),
    XOR("XOR"),
    NOT("NOT"),
    NAND("NAND"),
    NOR("NOR"),
    SHL("<<"),
    SHR(">>"),
    ROL("RoL"),
    ROR("RoR"),
    LEFT_PAREN("("),
    RIGHT_PAREN(")"),
    ANS("ANS"),
}

/**
 * 入力の最小単位（トークン）。
 */
sealed interface ProgToken {
    /**
     * 数値。入力中の文字列ではなく、語長で正規化済みの値を持つ。
     * 表示は基数によって変わるので、文字列にするのは ProgFormat の役目。
     */
    data class Num(val value: Long) : ProgToken

    /** 演算子・括弧・ANS */
    data class Sym(val symbol: ProgSymbol) : ProgToken
}
