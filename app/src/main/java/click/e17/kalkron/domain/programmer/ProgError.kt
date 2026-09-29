package click.e17.kalkron.domain.programmer

/**
 * プログラマーモードの計算で起きるエラーの種類。message は画面にそのまま出す文言。
 */
enum class ProgError(val message: String) {
    SYNTAX("式が正しくありません"),
    DIVISION_BY_ZERO("0 で割れません"),
    NEGATIVE_SHIFT("シフトの回数が負です"),
}

/** 計算を途中で打ち切るための例外 */
class ProgException(val error: ProgError) : Exception(error.message)
