package click.e17.kalkron.ui.info

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import click.e17.kalkron.BuildConfig
import click.e17.kalkron.R
import click.e17.kalkron.ui.components.GlassPanel
import click.e17.kalkron.ui.components.ModeHeader
import click.e17.kalkron.ui.theme.CalculatorTheme
import click.e17.kalkron.ui.theme.DigitText
import click.e17.kalkron.ui.theme.ElectricCyan
import click.e17.kalkron.ui.theme.Hairline
import click.e17.kalkron.ui.theme.MetaText
import click.e17.kalkron.ui.theme.OnSurfaceBright
import click.e17.kalkron.ui.theme.OnSurfaceMuted

/** 情報画面から開くリンク */
private const val PRIVACY_URL = "https://kalkron.e17.click/privacy.html"
private const val CONTACT_URL = "mailto:support@mail.e17.click"
private const val SOURCE_URL = "https://github.com/morststs/kalkron"

/**
 * 情報画面。バージョン、プライバシーポリシーなどのリンク、ライセンスの一覧を表示する。
 * 各画面のヘッダーの ⓘ から開く。
 */
@Composable
fun InfoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    InfoScreenContent(
        version = BuildConfig.VERSION_NAME,
        onBack = onBack,
        onOpenLink = { url ->
            // ブラウザやメールアプリが入っていない端末では開けないので、そのときは何もしない
            runCatching { uriHandler.openUri(url) }
        },
        // ライセンス全文は assets のテキストファイルから読む
        readLicense = { path -> context.assets.open(path).bufferedReader().use { it.readText() } },
        modifier = modifier,
    )
}

@Composable
fun InfoScreenContent(
    version: String,
    onBack: () -> Unit,
    onOpenLink: (String) -> Unit,
    readLicense: (String) -> String,
    modifier: Modifier = Modifier,
) {
    // 全文を開いているライセンスの名前（1つだけ開く）。画面を回転しても保つ
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.info_back),
                        tint = OnSurfaceMuted,
                    )
                }
                ModeHeader(mode = stringResource(R.string.mode_info))
            }
        }

        item {
            GlassPanel {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("VERSION", style = MaterialTheme.typography.labelSmall, color = MetaText)
                    Text(version, style = MaterialTheme.typography.headlineSmall, color = DigitText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.info_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceMuted,
                    )
                }
            }
        }

        item {
            GlassPanel {
                LinkRow(stringResource(R.string.info_privacy)) { onOpenLink(PRIVACY_URL) }
                HorizontalDivider(color = Hairline)
                LinkRow(stringResource(R.string.info_contact)) { onOpenLink(CONTACT_URL) }
                HorizontalDivider(color = Hairline)
                LinkRow(stringResource(R.string.info_source)) { onOpenLink(SOURCE_URL) }
            }
        }

        item {
            Text("LICENSES", style = MaterialTheme.typography.labelSmall, color = MetaText)
        }

        items(LICENSES, key = { it.name }) { entry ->
            val isOpen = expanded == entry.name
            GlassPanel {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button) { expanded = if (isOpen) null else entry.name }
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.name, style = MaterialTheme.typography.labelLarge, color = OnSurfaceBright)
                            Text(entry.detail, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceMuted)
                            Text(entry.licenseName, style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
                        }
                        Icon(
                            imageVector = if (isOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = OnSurfaceMuted,
                        )
                    }
                    if (isOpen) {
                        // 開いたときだけ読む。同じファイルを何度も読まないよう、パスごとに覚えておく
                        val text = remember(entry.licenseAsset) { readLicense(entry.licenseAsset) }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceMuted)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

/** タップで外部のページやメールを開く1行 */
@Composable
private fun LinkRow(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        color = ElectricCyan,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF050608, widthDp = 390, heightDp = 800)
@Composable
private fun InfoScreenPreview() {
    CalculatorTheme {
        InfoScreenContent(
            version = "1.2.1",
            onBack = {},
            onOpenLink = {},
            readLicense = { "Apache License\nVersion 2.0, January 2004" },
        )
    }
}
