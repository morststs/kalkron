package click.e17.kalkron.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import click.e17.kalkron.data.repository.HistoryRepository
import click.e17.kalkron.domain.CalculatorAction
import click.e17.kalkron.domain.CalculatorEngine
import click.e17.kalkron.domain.CalculatorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 電卓画面の ViewModel。
 *
 * 役割は次の 3 つだけで、計算そのものは [CalculatorEngine] に任せている。
 *   1. 画面の状態（UiState）を保持して公開する
 *   2. UI から来た操作をドメインに渡す
 *   3. 計算が完了したら Repository に履歴を保存する
 *
 * 画面回転などで Activity が作り直されても ViewModel は生き残るため、
 * 入力途中の状態が失われない。
 */
class CalculatorViewModel(
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    /** ドメインの内部状態。UI には公開しない */
    private var engineState: CalculatorState = CalculatorState()

    private val _uiState = MutableStateFlow(engineState.toUiState())

    /** 画面が購読する状態。外からは書き換えられないよう asStateFlow() で読み取り専用にする */
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    /**
     * キーが押されたときに UI から呼ばれる唯一の入口。
     */
    fun onAction(action: CalculatorAction) {
        val result = CalculatorEngine.reduce(engineState, action)
        engineState = result.state
        _uiState.value = result.state.toUiState()

        // 「= まで到達した計算」だけを履歴に残す
        val completed = result.completed ?: return
        viewModelScope.launch {
            historyRepository.save(completed)
        }
    }
}
