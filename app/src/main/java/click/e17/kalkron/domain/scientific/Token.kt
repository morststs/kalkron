package click.e17.kalkron.domain.scientific

/**
 * 記号のトークンの種類。text は画面に出す文字。
 */
enum class Symbol(val text: String) {
    PLUS("+"),
    MINUS("−"),
    TIMES("×"),
    DIVIDE("÷"),
    POWER("^"),
    SQUARE("²"),
    LEFT_PAREN("("),
    RIGHT_PAREN(")"),
    COMMA(","),
    X("x"),
    PI("π"),
    E("e"),
    ANS("ANS"),
    MEMORY("M"),
}

/**
 * 関数の種類。label は画面に出す名前、arity は引数の数。
 * d/dx と ∫ は第1引数に x を含む式を取る。
 */
enum class MathFunction(val label: String, val arity: Int) {
    SIN("sin", 1),
    COS("cos", 1),
    TAN("tan", 1),
    ASIN("sin⁻¹", 1),
    ACOS("cos⁻¹", 1),
    ATAN("tan⁻¹", 1),
    SINH("sinh", 1),
    COSH("cosh", 1),
    TANH("tanh", 1),
    ASINH("sinh⁻¹", 1),
    ACOSH("cosh⁻¹", 1),
    ATANH("tanh⁻¹", 1),
    LN("ln", 1),
    LOG("log", 1),
    SQRT("√", 1),
    EXP("exp", 1),
    DERIVATIVE("d/dx", 2),
    INTEGRAL("∫", 3),
}

/**
 * 入力の最小単位（トークン）。
 *
 * 入力を文字列ではなくトークンのリストで持つことで、⌫ で「sin(」を
 * まとめて消せるようにしている。text は画面に出すときの文字列。
 */
sealed interface Token {
    val text: String

    /** 数値。入力途中の "12." も持てるよう、文字列のまま保持する */
    data class Num(val digits: String) : Token {
        override val text: String get() = digits
    }

    /** 関数。開き括弧までを1つのトークンとして扱う */
    data class Fn(val function: MathFunction) : Token {
        override val text: String get() = "${function.label}("
    }

    /** 演算子・括弧・カンマ・定数などの記号 */
    data class Sym(val symbol: Symbol) : Token {
        override val text: String get() = symbol.text
    }
}
