package rs.gospaleks.waterspot.presentation.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReusableBottomSheetHost(
    show: Boolean,
    onDismissRequest: () -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit,
    allowPartial: Boolean = false,
    initialPartial: Boolean = false,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = !allowPartial)

    if (show) {
        // If partial expansion is allowed and requested, move to partial on first show
        LaunchedEffect(key1 = show) {
            if (allowPartial && initialPartial) {
                sheetState.partialExpand()
            }
        }
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .imePadding(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            tonalElevation = 4.dp,
            content = sheetContent
        )
    }
}