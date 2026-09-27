package click.e17.kalkron.ui.calculator.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import click.e17.kalkron.ui.components.hairlineBorder
import click.e17.kalkron.ui.theme.AmberTelemetry
import click.e17.kalkron.ui.theme.CyanDeep
import click.e17.kalkron.ui.theme.CyanPressFill
import click.e17.kalkron.ui.theme.DigitText
import click.e17.kalkron.ui.theme.ElectricCyan
import click.e17.kalkron.ui.theme.HairlineActive
import click.e17.kalkron.ui.theme.HairlineBottom
import click.e17.kalkron.ui.theme.HairlineTop
import click.e17.kalkron.ui.theme.KeycapBase

/**
 * キーの役割ごとの見た目。Stitch の "Action Tiers" に対応する。
 */
enum class CalculatorButtonStyle {
    /** 0〜9, . : 淡いグレーの文字 */
    Number,

    /** AC, DEL, %, +/- : アンバー。補助的・破壊的な操作 */
    Function,

    /** + - × ÷ : エレクトリックシアン */
    Operator,

    /** = : シアンで塗り潰し、常時うっすら発光させる */
    Accent,
}

/**
 * 電卓のキー 1 つ分（キーキャップ）。
 *
 * Stitch の指定に沿って次の状態を再現している。
 *  - 通常時: 暗いガラスの地に、上辺が明るいヘアライン枠
 *  - 押下時: 内側がシアンで満たされ、枠が発光する（潰れる動きはあえて付けない）
 *  - `=` のみ: 常にシアンで塗られ、外側に光がにじむ
 */
@Composable
fun CalculatorButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: CalculatorButtonStyle = CalculatorButtonStyle.Number,
) {
    val shape = MaterialTheme.shapes.small
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val isAccent = style == CalculatorButtonStyle.Accent
    val contentColor = when (style) {
        CalculatorButtonStyle.Number -> DigitText
        CalculatorButtonStyle.Function -> AmberTelemetry
        CalculatorButtonStyle.Operator -> ElectricCyan
        CalculatorButtonStyle.Accent -> CyanDeep
    }
    val containerColor = when {
        isAccent -> ElectricCyan
        pressed -> CyanPressFill
        else -> KeycapBase
    }

    Box(
        modifier = modifier
            // 発光は影の色で表現する（色付きの影は API 28 以降で有効）
            .shadow(
                elevation = if (isAccent || pressed) 12.dp else 0.dp,
                shape = shape,
                ambientColor = ElectricCyan,
                spotColor = ElectricCyan,
            )
            .clip(shape)
            .background(containerColor)
            .then(
                if (isAccent) {
                    Modifier
                } else {
                    // 押している間だけ枠をシアンに切り替える
                    Modifier.hairlineBorder(
                        shape = shape,
                        top = if (pressed) HairlineActive else HairlineTop,
                        bottom = if (pressed) HairlineActive else HairlineBottom,
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = if (isAccent) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.labelLarge
            },
            color = contentColor,
        )
    }
}
