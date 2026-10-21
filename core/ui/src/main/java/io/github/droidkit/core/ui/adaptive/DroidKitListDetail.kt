package io.github.droidkit.core.ui.adaptive

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

/**
 * Two-pane list-detail shell used at expanded width (plan
 * 2026-10-20-responsive-adaptive-ui). Wraps [ListDetailPaneScaffold] so feature
 * screens don't repeat the navigator ceremony:
 * - [listPane] receives an `onSelect(key)` that opens the detail pane.
 * - [detailPane] receives the selected key and an `onBack` that collapses back
 *   to the list-only pane — wire it to the embedded screen's Up affordance.
 *
 * Compact/medium windows keep the existing list→push→detail navigation; routes
 * and deep links are untouched.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun <T : Any> DroidKitListDetail(
    listPane: @Composable (onSelect: (T) -> Unit) -> Unit,
    detailPane: @Composable (contentKey: T, onBack: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<T>()
    val scope = rememberCoroutineScope()
    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        modifier = modifier,
        listPane = {
            listPane { key ->
                scope.launch {
                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, key)
                }
            }
        },
        detailPane = {
            navigator.currentDestination?.contentKey?.let { key ->
                detailPane(key) {
                    scope.launch { navigator.navigateBack() }
                }
            }
        },
    )
}
