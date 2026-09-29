package click.e17.kalkron.data.local

import click.e17.kalkron.domain.CalculationRecord
import click.e17.kalkron.domain.CalculatorMode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * DB の行（Entity）とドメインモデルの相互変換のテスト。
 * モードは列に名前（文字列）で保存する。
 */
class CalculationHistoryMappingTest {

    @Test
    fun `モードは名前で保存され名前から復元される`() {
        val record = CalculationRecord(
            id = 1,
            expression = "sin(0)",
            result = "0",
            createdAt = 10,
            mode = CalculatorMode.SCIENTIFIC,
        )

        val entity = record.toEntity()

        assertEquals("SCIENTIFIC", entity.mode)
        assertEquals(record, entity.toDomain())
    }

    @Test
    fun `未知のモード名はSTANDARDとして読む`() {
        // 将来のバージョンで増えたモード名を古いアプリが読んでも落ちないようにする
        val entity = CalculationHistoryEntity(
            id = 1,
            expression = "1 + 1",
            result = "2",
            createdAt = 10,
            mode = "FUTURE_MODE",
        )

        assertEquals(CalculatorMode.STANDARD, entity.toDomain().mode)
    }

    @Test
    fun `プログラマーのモードも名前で保存され復元される`() {
        val record = CalculationRecord(
            id = 2,
            expression = "FF AND F",
            result = "F (HEX)",
            createdAt = 20,
            mode = CalculatorMode.PROGRAMMER,
        )
        assertEquals("PROGRAMMER", record.toEntity().mode)
        assertEquals(record, record.toEntity().toDomain())
    }
}
