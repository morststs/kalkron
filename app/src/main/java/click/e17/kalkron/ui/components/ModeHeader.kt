package click.e17.kalkron.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.IconButton
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource
import click.e17.kalkron.R
import click.e17.kalkron.ui.theme.OnSurfaceMuted
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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
/**
 * ヘッダーの ⓘ を押したときに情報画面を開く処理。
 *
 * ヘッダーは4つの画面の縦・横それぞれで使っているため、引数で渡すと全画面の関数を書き換えることになる。
 * CompositionLocal を使うと、画面遷移を定義している CalculatorApp から、途中の画面を経由せずに直接届けられる。
 * 渡されていない（null の）ときは ⓘ を出さない（プレビューや情報画面自身）。
 */
val LocalOpenInfo = staticCompositionLocalOf<(() -> Unit)?> { null }

@Composable
fun ModeHeader(
    mode: String,
    modifier: Modifier = Modifier,
) {
    val openInfo = LocalOpenInfo.current
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
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                tint = CyanDeep,
                modifier = Modifier.size(19.dp),
            )
        }

        Column {
            Text(
                // 末尾の _ はロゴの >_ と同じ入力待ちのカーソルに見立て、ロゴと同じシアンにする
                text = buildAnnotatedString {
                    append("KALKRON")
                    withStyle(SpanStyle(color = ElectricCyan)) { append("_") }
                },
                style = MaterialTheme.typography.labelLarge,
                color = OnSurfaceBright,
            )
            Text(
                text = mode,
                style = MaterialTheme.typography.labelSmall,
                color = MetaText,
            )
        }

        if (openInfo != null) {
            Spacer(modifier = Modifier.weight(1f))
            // IconButton は既定で 48dp 四方を確保し、ヘッダーが高くなって PROG 画面などの下の表示を押し出す。
            // ロゴタイル（30dp）に合わせて 32dp に収める
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                IconButton(onClick = openInfo, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.info_open),
                        tint = OnSurfaceMuted,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
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
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 先頭の小さなランプ（デザイン画の帯の頭にある点）
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(ElectricCyan)
        )
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
