package click.e17.kalkron.ui.scientific

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import click.e17.kalkron.data.repository.HistoryRepository
import click.e17.kalkron.domain.scientific.ScientificAction
import click.e17.kalkron.domain.scientific.ScientificEngine
import click.e17.kalkron.domain.scientific.ScientificState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 関数電卓画面の ViewModel。
 * 標準モードの CalculatorViewModel と同じ形で、計算は ScientificEngine に任せる。
 */
class ScientificViewModel(
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private var engineState = ScientificState()

    private val _uiState = MutableStateFlow(engineState.toUiState())
    val uiState: StateFlow<ScientificUiState> = _uiState.asStateFlow()

    fun onAction(action: ScientificAction) {
        val result = ScientificEngine.reduce(engineState, action)
        engineState = result.state
        _uiState.value = result.state.toUiState()

        val completed = result.completed ?: return
        viewModelScope.launch {
            historyRepository.save(completed)
        }
    }
}
