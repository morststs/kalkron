package click.e17.kalkron.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import click.e17.kalkron.ui.theme.CyanDeep
import click.e17.kalkron.ui.theme.ElectricCyan
import click.e17.kalkron.ui.theme.MetaText
import click.e17.kalkron.ui.theme.OnSurfaceBright

/**
 * 画面上部の見出し。Stitch のヘッダー（ロゴタイル + システム名 + モード名）を再現。
 *
 * 見出しは Space Grotesk・Latin のみで組む。日本語を混ぜると
 * 代替フォントに落ちて字面が揃わないため、ここは英字表記にしている
 * （アプリ名の「電卓」はランチャーのラベルとして別に持っている）。
 */
@Composable
fun ModeHeader(
    mode: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // ロゴタイル
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(MaterialTheme.shapes.small)
                .background(ElectricCyan),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "=",
                style = MaterialTheme.typography.headlineMedium,
                color = CyanDeep,
            )
        }

        Column {
            Text(
                text = "KALKRON.SYS",
                style = MaterialTheme.typography.labelLarge,
                color = OnSurfaceBright,
            )
            Text(
                text = mode,
                style = MaterialTheme.typography.labelSmall,
                color = MetaText,
            )
        }
    }
}

/**
 * ヘッダー下の細い情報帯。
 *
 * Stitch の "BUS: 64-BIT ALIGN | CORE: OK | LATENCY: 0.12ms" にあたる部分だが、
 * 意味のない数字を並べても仕方がないので、実際の状態を表示している。
 */
@Composable
fun TelemetryStrip(
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { (label, value) ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "$label:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MetaText,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan,
                )
            }
        }
    }
}
