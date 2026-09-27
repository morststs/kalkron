package click.e17.kalkron.ui.calculator.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import click.e17.kalkron.domain.CalculatorEngine
import click.e17.kalkron.ui.calculator.CalculatorUiState
import click.e17.kalkron.ui.components.GlassPanel
import click.e17.kalkron.ui.components.hairlineBorder
import click.e17.kalkron.ui.theme.ElectricCyan
import click.e17.kalkron.ui.theme.ErrorRed
import click.e17.kalkron.ui.theme.MetaText
import click.e17.kalkron.ui.theme.OnSurfaceMuted

/**
 * 画面上部の表示部。Stitch の "The Readout (HUD Canvas)" を再現したもの。
 *
 * 上段に途中式を小さく、下段に現在値を大きなシアンの等幅数字で出す 2 段構成。
 * 周囲のチップには、飾りではなく実際の計算エンジンの仕様を表示している。
 */
@Composable
fun CalculatorDisplay(
    uiState: CalculatorUiState,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val scrollState = rememberScrollState()

    // 表示が変わるたびに右端（最新の桁）までスクロールする
    LaunchedEffect(uiState.display) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    GlassPanel(modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

            // --- チップ列: 左に計算方式、右に状態ランプ ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HudChip(text = "DEC", accent = true)
                    HudChip(text = "BIGDECIMAL")
                }
                StatusLamp(isError = uiState.isError)
            }

            Spacer(modifier = Modifier.height(if (compact) 8.dp else 20.dp))

            // --- 途中式 ---
            Text(
                text = uiState.expression,
                style = MaterialTheme.typography.bodyMedium,
                color = MetaText,
                maxLines = 1,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )

            // --- 現在値 ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = uiState.display,
                    style = if (compact) {
                        MaterialTheme.typography.displayMedium
                    } else {
                        MaterialTheme.typography.displayLarge
                    },
                    color = if (uiState.isError) ErrorRed else ElectricCyan,
                    maxLines = 1,
                    softWrap = false,
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 8.dp else 16.dp))

            // --- 脚注: 計算エンジンの実際の仕様 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "MAX ${CalculatorEngine.MAX_INPUT_DIGITS} DIGITS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MetaText,
                )
                Text(
                    text = "ROUND: HALF-UP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MetaText,
                )
            }
        }
    }
}

/** HUD 上の小さなラベル。枠だけの四角に等幅の小文字を入れる */
@Composable
private fun HudChip(
    text: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    val shape = MaterialTheme.shapes.extraSmall
    Box(
        modifier = modifier
            .clip(shape)
            .hairlineBorder(shape)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (accent) ElectricCyan else OnSurfaceMuted,
        )
    }
}

/** 右上の状態ランプ。エラー時だけ赤く光らせる */
@Composable
private fun StatusLamp(
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    val color: Color = if (isError) ErrorRed else ElectricCyan
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = if (isError) "ERROR" else "READY",
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}
