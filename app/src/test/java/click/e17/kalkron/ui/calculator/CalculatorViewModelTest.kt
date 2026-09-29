package click.e17.kalkron.ui.calculator

import click.e17.kalkron.MainDispatcherRule
import click.e17.kalkron.domain.CalculatorAction
import click.e17.kalkron.domain.CalculatorMode
import click.e17.kalkron.domain.Operator
import click.e17.kalkron.fake.FakeHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * ViewModel のテスト。
 *
 * 計算そのものは CalculatorEngineTest で検証済みなので、ここでは
 * 「UI に見せる状態が正しく更新されるか」「履歴が保存されるか」を確認する。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeHistoryRepository(now = { 1_700_000_000_000L })
    private val viewModel = CalculatorViewModel(repository)

    @Test
    fun `初期状態は0を表示する`() {
        assertEquals("0", viewModel.uiState.value.display)
        assertEquals("", viewModel.uiState.value.expression)
    }

    @Test
    fun `キー操作が表示に反映される`() {
        viewModel.onAction(CalculatorAction.Digit(4))
        viewModel.onAction(CalculatorAction.Digit(2))

        assertEquals("42", viewModel.uiState.value.display)
    }

    @Test
    fun `演算子を押すと途中式が表示される`() {
        viewModel.onAction(CalculatorAction.Digit(7))
        viewModel.onAction(CalculatorAction.Operate(Operator.MULTIPLY))

        assertEquals("7 ×", viewModel.uiState.value.expression)
    }

    @Test
    fun `計算が完了すると履歴が保存される`() = runTest {
        viewModel.onAction(CalculatorAction.Digit(6))
        viewModel.onAction(CalculatorAction.Operate(Operator.MULTIPLY))
        viewModel.onAction(CalculatorAction.Digit(7))
        viewModel.onAction(CalculatorAction.Equals)

        assertEquals("42", viewModel.uiState.value.display)
        assertEquals(1, repository.saved.size)
        assertEquals("6 × 7", repository.saved.first().expression)
        assertEquals("42", repository.saved.first().result)
        assertEquals(1_700_000_000_000L, repository.saved.first().createdAt)
    }

    @Test
    fun `計算が完了していなければ履歴は保存されない`() = runTest {
        viewModel.onAction(CalculatorAction.Digit(6))
        viewModel.onAction(CalculatorAction.Operate(Operator.MULTIPLY))

        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun `0除算するとエラー状態になる`() = runTest {
        viewModel.onAction(CalculatorAction.Digit(6))
        viewModel.onAction(CalculatorAction.Operate(Operator.DIVIDE))
        viewModel.onAction(CalculatorAction.Digit(0))
        viewModel.onAction(CalculatorAction.Equals)

        assertTrue(viewModel.uiState.value.isError)
        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun `標準モードの計算はSTANDARDとして保存される`() = runTest {
        viewModel.onAction(CalculatorAction.Digit(2))
        viewModel.onAction(CalculatorAction.Operate(Operator.ADD))
        viewModel.onAction(CalculatorAction.Digit(3))
        viewModel.onAction(CalculatorAction.Equals)

        assertEquals(CalculatorMode.STANDARD, repository.saved.single().mode)
    }
}
