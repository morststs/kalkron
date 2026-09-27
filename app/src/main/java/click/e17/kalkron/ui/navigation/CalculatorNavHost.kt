package click.e17.kalkron.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import click.e17.kalkron.R
import click.e17.kalkron.ui.calculator.CalculatorScreen
import click.e17.kalkron.ui.history.HistoryScreen
import click.e17.kalkron.ui.theme.CyanDeep
import click.e17.kalkron.ui.theme.ElectricCyan
import click.e17.kalkron.ui.theme.Hairline
import click.e17.kalkron.ui.theme.MetaText
import click.e17.kalkron.ui.theme.ObsidianSurface
import click.e17.kalkron.ui.theme.ObsidianSurfaceLowest
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * 画面（遷移先）の定義。
 *
 * 文字列のルート名ではなく @Serializable なオブジェクトを使う「型安全ナビゲーション」。
 * 遷移先や引数の書き間違いをコンパイル時に検出できる。
 */
@Serializable
data object CalculatorRoute

@Serializable
data object HistoryRoute

/** 下部ナビに並べる項目 */
private data class NavItem(
    val labelRes: Int,
    val icon: ImageVector,
    val route: Any,
    val routeClass: KClass<*>,
)

private val NAV_ITEMS = listOf(
    NavItem(R.string.nav_calculator, Icons.Default.Calculate, CalculatorRoute, CalculatorRoute::class),
    NavItem(R.string.nav_history, Icons.Default.History, HistoryRoute, HistoryRoute::class),
)

/**
 * アプリ全体の骨組み。下部ナビと画面遷移をここで定義する。
 *
 * Stitch のデザインが下部ナビでモードを切り替える構成なので、
 * それに合わせて電卓（STD）と履歴（HIST）の 2 つを並べている。
 */
@Composable
fun CalculatorApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        modifier = modifier,
        containerColor = ObsidianSurface,
        bottomBar = {
            Column {
                HorizontalDivider(thickness = 1.dp, color = Hairline)
                NavigationBar(
                    containerColor = ObsidianSurfaceLowest,
                    tonalElevation = 0.dp,
                ) {
                    NAV_ITEMS.forEach { item ->
                        // 現在どの画面にいるかを型安全に判定する
                        val selected = currentDestination?.hierarchy?.any {
                            it.hasRoute(item.routeClass)
                        } == true

                        val label = stringResource(item.labelRes)

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // タブを行き来しても履歴が積み上がらないようにする
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = label,
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanDeep,
                                selectedTextColor = ElectricCyan,
                                indicatorColor = ElectricCyan,
                                unselectedIconColor = MetaText,
                                unselectedTextColor = MetaText,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CalculatorRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<CalculatorRoute> { CalculatorScreen() }
            composable<HistoryRoute> { HistoryScreen() }
        }
    }
}
