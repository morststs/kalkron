package click.e17.kalkron.ui.programmer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import click.e17.kalkron.data.repository.HistoryRepository
import click.e17.kalkron.domain.programmer.ProgrammerAction
import click.e17.kalkron.domain.programmer.ProgrammerEngine
import click.e17.kalkron.domain.programmer.ProgrammerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * プログラマーモード画面の ViewModel。
 * 標準・関数電卓の ViewModel と同じ形で、計算は ProgrammerEngine に任せる。
 */
class ProgrammerViewModel(
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private var engineState = ProgrammerState()

    private val _uiState = MutableStateFlow(engineState.toUiState())
    val uiState: StateFlow<ProgrammerUiState> = _uiState.asStateFlow()

    fun onAction(action: ProgrammerAction) {
        val result = ProgrammerEngine.reduce(engineState, action)
        engineState = result.state
        _uiState.value = result.state.toUiState()

        val completed = result.completed ?: return
        viewModelScope.launch {
            historyRepository.save(completed)
        }
    }
}
