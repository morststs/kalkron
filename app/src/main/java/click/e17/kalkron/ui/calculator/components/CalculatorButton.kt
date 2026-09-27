package click.e17.kalkron.ui.calculator.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import click.e17.kalkron.ui.components.hairlineBorder
import click.e17.kalkron.ui.theme.CyanDeep
import click.e17.kalkron.ui.theme.CyanPressFill
import click.e17.kalkron.ui.theme.DigitText
import click.e17.kalkron.ui.theme.ElectricCyan
import click.e17.kalkron.ui.theme.HairlineActive
import click.e17.kalkron.ui.theme.HairlineBottom
import click.e17.kalkron.ui.theme.HairlineTop
import click.e17.kalkron.ui.theme.KeycapBase
import click.e17.kalkron.ui.theme.KeycapHover
import click.e17.kalkron.ui.theme.KeycapMarker

/**
 * キーの役割ごとの見た目。
 */
enum class CalculatorButtonStyle {
    /** 0〜9, . : 大きめの数字 */
    Number,

    /** AC, DEL, %, +/- : 小さめの文字またはアイコン */
    Function,

    /** + - × ÷ : シアン。地を一段明るくして列として目立たせる */
    Operator,

    /** = : シアンで塗り潰し、常時うっすら発光させる */
    Accent,
}

/**
 * 電卓のキー 1 つ分（キーキャップ）。
 *
 * [label] か [icon] のどちらかを表示する。
 */
@Composable
fun CalculatorButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: CalculatorButtonStyle = CalculatorButtonStyle.Number,
    icon: ImageVector? = null,
) {
    // キーの角丸はデザイン画に合わせて 8dp（パネルと同じ large）
    val shape = MaterialTheme.shapes.large
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val isAccent = style == CalculatorButtonStyle.Accent
    val contentColor = when (style) {
        // AC・DEL なども白系にする（アンバーはエラー表示と履歴削除のみに使う）
        CalculatorButtonStyle.Number, CalculatorButtonStyle.Function -> DigitText
        CalculatorButtonStyle.Operator -> ElectricCyan
        CalculatorButtonStyle.Accent -> CyanDeep
    }
    val containerColor = when {
        isAccent -> ElectricCyan
        pressed -> CyanPressFill
        // 演算子の列だけ地を明るくして、数字と見分けやすくする
        style == CalculatorButtonStyle.Operator -> KeycapHover
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
        // 数字キーの左上に置く小さな点（デザイン画にあるキーキャップの刻印）
        if (style == CalculatorButtonStyle.Number) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 7.dp, top = 7.dp)
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(KeycapMarker)
            )
        }

        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
        } else {
            Text(
                text = label,
                // 役割ごとに文字の大きさを変える。数字と演算子はしっかり大きく取る
                style = when (style) {
                    CalculatorButtonStyle.Number ->
                        MaterialTheme.typography.labelLarge.copy(fontSize = 22.sp)

                    // ÷ や − は字面が小さく見えるため、数字より大きめに取る
                    CalculatorButtonStyle.Operator, CalculatorButtonStyle.Accent ->
                        MaterialTheme.typography.labelLarge.copy(fontSize = 32.sp)

                    CalculatorButtonStyle.Function ->
                        MaterialTheme.typography.labelLarge
                },
                color = contentColor,
            )
        }
    }
}
