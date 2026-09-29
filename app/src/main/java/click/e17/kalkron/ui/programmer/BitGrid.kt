package click.e17.kalkron.ui.programmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import click.e17.kalkron.ui.components.hairlineBorder
import click.e17.kalkron.ui.theme.CyanDeep
import click.e17.kalkron.ui.theme.ElectricCyan
import click.e17.kalkron.ui.theme.KeycapBase
import click.e17.kalkron.ui.theme.MetaText
import click.e17.kalkron.ui.theme.OnSurfaceMuted

/** 1ページに並べるビット数（8マス × 2行） */
private const val BITS_PER_PAGE = 16

/**
 * ビット反転グリッド。いま編集している数のビットを 0/1 のマスで表示し、タップで反転する。
 *
 * 左が上位ビット。8ビットは1行、16ビット以上は1ページ16ビットで « » で送る。
 * ページはこの画面だけの表示状態なので、ViewModel ではなくここで持つ。
 *
 * @param bits 添字がビット番号（0 が最下位）。要素数は語長
 */
@Composable
fun BitGrid(
    bits: List<Boolean>,
    onFlip: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pageCount = maxOf(1, bits.size / BITS_PER_PAGE)
    // 語長（ビット数）が変わったら最下位のページに戻す。キーに語長を渡すと、値が変わったときに作り直される。
    // 64→8→64 と切り替えたとき、以前開いていた上位のページが突然表示されるのを防ぐ
    var page by rememberSaveable(bits.size) { mutableIntStateOf(0) }
    val current = page.coerceIn(0, pageCount - 1)

    val low = current * BITS_PER_PAGE
    val rows: List<List<Int>> = if (bits.size < BITS_PER_PAGE) {
        listOf((bits.size - 1 downTo 0).toList())
    } else {
        listOf((low + 15 downTo low + 8).toList(), (low + 7 downTo low).toList())
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "BITFIELD (${rows.first().first()} … ${rows.last().last()})",
                style = MaterialTheme.typography.labelSmall,
                color = MetaText,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (pageCount > 1) {
                PageArrow(label = "« LSB", enabled = current > 0, onClick = { page = current - 1 })
                PageArrow(label = "MSB »", enabled = current < pageCount - 1, onClick = { page = current + 1 })
            }
        }
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { index ->
                    BitCell(
                        index = index,
                        set = bits[index],
                        onClick = { onFlip(index) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PageArrow(label: String, enabled: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = if (enabled) ElectricCyan else MetaText,
        modifier = Modifier
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

/** 1ビット分のマス。1 ならシアンで塗り、左上にビット番号を小さく添える */
@Composable
private fun BitCell(index: Int, set: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.small
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(shape)
            .background(if (set) ElectricCyan else KeycapBase)
            .then(if (set) Modifier else Modifier.hairlineBorder(shape))
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Text(
            text = index.toString(),
            // 行の高さと字間も詰め、中央の 0 / 1 と重ならないようにする
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                lineHeight = 10.sp,
                letterSpacing = 0.sp,
            ),
            color = if (set) CyanDeep else MetaText,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 3.dp, top = 1.dp),
        )
        Text(
            text = if (set) "1" else "0",
            style = MaterialTheme.typography.labelMedium,
            color = if (set) CyanDeep else OnSurfaceMuted,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
