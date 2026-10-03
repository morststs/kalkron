package click.e17.kalkron.ui.programmer

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import click.e17.kalkron.domain.programmer.Radix

/**
 * 空白で区切った文字列を、区切りの数ができるだけ同じになるように lineCount 行に分ける。
 * 割り切れないときは上の行を長くする。BIN（4ビットごとに空白）を上下にそろえて並べるために使う。
 */
fun splitGroups(text: String, lineCount: Int): List<String> {
    val groups = text.split(" ")
    if (lineCount <= 1 || groups.size < lineCount) return listOf(text)
    val perLine = (groups.size + lineCount - 1) / lineCount
    return groups.chunked(perLine).map { it.joinToString(" ") }
}

/**
 * 基数1行分の値。与えられた幅に収まるように、行数と文字の大きさを決めて表示する。
 *
 * 1. 1行で元の大きさのまま収まれば、そのまま1行
 * 2. 収まらない BIN は、真ん中の区切りで上下2行に分ける（上下の長さがそろう）
 * 3. それでも入らなければ、すべての行を同じ割合で縮める
 *
 * 64ビットの BIN は空白込みで79文字あり、スマホの幅では 1行に収めると 5sp ほどになってしまうため。
 */
@Composable
fun RadixValue(
    radix: Radix,
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier = modifier) {
        val available = constraints.maxWidth
        // 文字の幅を実際に測る。等幅フォントでも字間の設定があるので計算より測るほうが確か
        fun widthOf(line: String) = measurer.measure(line, style, softWrap = false, maxLines = 1).size.width

        val lines = if (radix == Radix.BIN && widthOf(text) > available) splitGroups(text, 2) else listOf(text)
        val widest = lines.maxOf { widthOf(it) }
        // 大きさを縮める割合。丸めの誤差で1文字はみ出さないよう、少しだけ余裕を持たせる
        val scale = if (widest <= available || widest == 0) 1f else available * 0.98f / widest
        val fitted = style.copy(fontSize = style.fontSize * scale)

        Column {
            lines.forEach { line ->
                Text(text = line, style = fitted, color = color, maxLines = 1, softWrap = false)
            }
        }
    }
}
