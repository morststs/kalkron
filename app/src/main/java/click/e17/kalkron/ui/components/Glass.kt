package click.e17.kalkron.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import click.e17.kalkron.ui.theme.HairlineBottom
import click.e17.kalkron.ui.theme.HairlineTop
import click.e17.kalkron.ui.theme.PanelGlass

/**
 * Stitch のデザインで全面的に使われている「ヘアライン枠」。
 *
 * 上辺を明るく、下辺に向かって消える 1px のグラデーション枠を引くことで、
 * 上方にある仮想の光源を反射しているように見せる。
 * 影を落とさずに立体感を出すのがこのデザインの肝。
 */
fun Modifier.hairlineBorder(
    shape: Shape,
    top: Color = HairlineTop,
    bottom: Color = HairlineBottom,
): Modifier = border(
    width = 1.dp,
    brush = Brush.verticalGradient(listOf(top, bottom)),
    shape = shape,
)

/**
 * 黒曜石のガラス板。HUD 表示部や履歴カードの土台に使う。
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(PanelGlass)
            .hairlineBorder(shape),
        content = content,
    )
}
