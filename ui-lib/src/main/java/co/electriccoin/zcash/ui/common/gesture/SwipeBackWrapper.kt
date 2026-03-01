package co.electriccoin.zcash.ui.common.gesture

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeBackWrapper(
    onBack: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val screenWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { screenWidthDp.dp.toPx() }
    val threshold = screenWidthPx * 0.3f

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value > threshold) {
                                    offsetX.animateTo(screenWidthPx, tween(200))
                                    onBack()
                                } else {
                                    offsetX.animateTo(0f, tween(200))
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetX.animateTo(0f, tween(200)) }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newValue = (offsetX.value + dragAmount).coerceAtLeast(0f)
                                offsetX.snapTo(newValue)
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}
