package org.cru.godtools.ui.dashboard.personalization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Density

@Composable
internal fun rememberFloatLastItemsToBottomArrangement(numToFloat: Int) =
    remember(numToFloat) { FloatLastItemsToBottomArrangement(numToFloat) }

internal class FloatLastItemsToBottomArrangement(val numToFloat: Int) : Arrangement.Vertical {
    override fun Density.arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) {
        var currentOffset = 0
        sizes.forEachIndexed { index, size ->
            outPositions[index] = currentOffset
            currentOffset += size
        }

        if (currentOffset < totalSize && numToFloat > 0) {
            currentOffset = totalSize
            sizes.takeLast(numToFloat).reversed().forEachIndexed { index, size ->
                currentOffset -= size
                outPositions[sizes.lastIndex - index] = currentOffset
            }
        }
    }
}
