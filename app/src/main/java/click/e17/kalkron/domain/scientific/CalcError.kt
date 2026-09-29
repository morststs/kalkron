package click.e17.kalkron.domain.scientific

/**
 * 関数電卓の計算で起きるエラーの種類。message は画面にそのまま出す文言。
 */
enum class CalcError(val message: String) {
    SYNTAX("式が正しくありません"),
    DIVISION_BY_ZERO("0 で割れません"),
    DOMAIN("定義域の外です"),
    OVERFLOW("オーバーフロー"),
    INTEGRAL_NOT_CONVERGED("積分が収束しません"),
    VARIABLE_OUTSIDE_CALCULUS("x は d/dx・∫ の中でのみ使えます"),
}

/** 計算を途中で打ち切るための例外。どの種類のエラーかを持つ */
class CalcException(val error: CalcError) : Exception(error.message)
