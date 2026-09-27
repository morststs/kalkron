package click.e17.kalkron

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import click.e17.kalkron.ui.navigation.CalculatorApp
import click.e17.kalkron.ui.theme.CalculatorTheme
import click.e17.kalkron.ui.theme.ObsidianSurface

/**
 * アプリで唯一の Activity。
 *
 * Compose を使う構成では Activity は「Compose の描画を始める入口」だけを担当し、
 * 画面ごとの処理は Composable と ViewModel に書く（Single Activity 構成）。
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // システムバーの裏まで描画する。
        // ダーク固定のデザインなので、バーのアイコンも明色で固定する
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        setContent {
            CalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianSurface,
                ) {
                    CalculatorApp()
                }
            }
        }
    }
}
