package click.e17.kalkron.domain.programmer

/**
 * 語長（1つの値を何ビットで表すか）。
 *
 * 値は常に Long（64ビット）で持ち、演算のたびに normalize で語長の範囲に切り詰める。
 * CPU のレジスタと同じく、あふれた上位ビットは捨てる（桁あふれは回り込む）。
 */
enum class WordSize(val bits: Int) {
    QWORD(64),
    DWORD(32),
    WORD(16),
    BYTE(8),
    ;

    /** 語長のビットがすべて 1 のマスク */
    val mask: Long
        get() = if (bits == 64) -1L else (1L shl bits) - 1

    /**
     * 語長へ正規化する。
     * 符号ありは語長の最上位ビットを符号として上に広げ（符号拡張）、符号なしは上のビットを 0 にする。
     */
    fun normalize(value: Long, signed: Boolean): Long = when {
        bits == 64 -> value
        signed -> (value shl (64 - bits)) shr (64 - bits)
        else -> value and mask
    }

    /** 語長で切り詰めたビットの並び（符号なしとして見た値） */
    fun pattern(value: Long): Long = value and mask

    fun maxSigned(): Long = if (bits == 64) Long.MAX_VALUE else (1L shl (bits - 1)) - 1

    fun minSigned(): Long = if (bits == 64) Long.MIN_VALUE else -(1L shl (bits - 1))
}
