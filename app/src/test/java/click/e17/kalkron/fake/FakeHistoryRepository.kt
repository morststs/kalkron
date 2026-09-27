package click.e17.kalkron.fake

import click.e17.kalkron.data.repository.HistoryRepository
import click.e17.kalkron.domain.Calculation
import click.e17.kalkron.domain.CalculationRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * テスト用の偽 Repository。
 *
 * Room（＝Android の実機能）を使わずにメモリ上で完結するので、
 * ViewModel のテストを JVM 上で動かせる。
 * 本物と同じ [HistoryRepository] を実装しているため差し替えが可能——
 * これが「Repository を interface にしておく」ことの実利。
 */
class FakeHistoryRepository(
    private val now: () -> Long = { 0L },
) : HistoryRepository {

    private val records = MutableStateFlow<List<CalculationRecord>>(emptyList())

    /** テストから中身を確認するための窓口 */
    val saved: List<CalculationRecord> get() = records.value

    override fun observeHistory(limit: Int): Flow<List<CalculationRecord>> =
        records.map { list -> list.sortedByDescending { it.createdAt }.take(limit) }

    override suspend fun save(calculation: Calculation) {
        records.value = records.value + CalculationRecord(
            id = records.value.size + 1L,
            expression = calculation.expression,
            result = calculation.result,
            createdAt = now(),
        )
    }

    override suspend fun clear() {
        records.value = emptyList()
    }
}
