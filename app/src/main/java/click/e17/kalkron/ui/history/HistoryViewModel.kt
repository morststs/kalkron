package click.e17.kalkron.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import click.e17.kalkron.data.repository.HistoryRepository
import click.e17.kalkron.domain.CalculationRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 履歴画面の状態 */
data class HistoryUiState(
    val records: List<CalculationRecord> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * 履歴画面の ViewModel。
 *
 * Repository が返す Flow を stateIn で StateFlow に変換している。
 * DB が更新されると Room → Repository → ViewModel → 画面の順に自動で伝わるので、
 * 「再読み込み」のようなコードを自分で書く必要がない。
 */
class HistoryViewModel(
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = historyRepository.observeHistory()
        .map { records -> HistoryUiState(records = records, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            // 画面が見えなくなって 5 秒経ったら購読を止める（無駄な DB 監視を避ける）
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(),
        )

    fun onClearHistory() {
        viewModelScope.launch {
            historyRepository.clear()
        }
    }
}
