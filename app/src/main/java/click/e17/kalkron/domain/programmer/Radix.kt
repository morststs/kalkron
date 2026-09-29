package click.e17.kalkron.domain.programmer

/**
 * 基数（何進数で表示・入力するか）。
 */
enum class Radix(val base: Int, val label: String) {
    HEX(16, "HEX"),
    DEC(10, "DEC"),
    OCT(8, "OCT"),
    BIN(2, "BIN"),
    ;

    /** この基数で入力できる数字か（digit は 0〜15） */
    fun accepts(digit: Int): Boolean = digit in 0 until base

    /**
     * 区切りなしの文字列。
     * DEC は符号の有無に従い、それ以外は語長で切り詰めたビットの並びを符号なしとして表す。
     */
    fun plain(value: Long, wordSize: WordSize, signed: Boolean): String = when (this) {
        DEC -> if (signed) value.toString() else value.toULong().toString()
        else -> java.lang.Long.toUnsignedString(wordSize.pattern(value), base).uppercase()
    }

    /** 表示用の文字列。DEC は3桁区切り、BIN は4ビットごとに空白を入れる */
    fun grouped(value: Long, wordSize: WordSize, signed: Boolean): String {
        val text = plain(value, wordSize, signed)
        return when (this) {
            DEC -> groupFromRight(text, size = 3, separator = ",")
            BIN -> groupFromRight(text, size = 4, separator = " ")
            HEX, OCT -> text
        }
    }

    /** 右から size 桁ごとに区切りを入れる。先頭の "-" は区切りの対象にしない */
    private fun groupFromRight(text: String, size: Int, separator: String): String {
        val negative = text.startsWith("-")
        val digits = if (negative) text.drop(1) else text
        val grouped = digits.reversed().chunked(size).joinToString(separator).reversed()
        return if (negative) "-$grouped" else grouped
    }
}
