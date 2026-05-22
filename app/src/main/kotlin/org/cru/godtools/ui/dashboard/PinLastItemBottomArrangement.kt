package org.cru.godtools.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Density

@Composable
internal fun rememberPinLastItemBottomArrangement(): Arrangement.Vertical = remember {
    object : Arrangement.Vertical {
        override fun Density.arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) {
            var currentOffset = 0
            sizes.forEachIndexed { index, size ->
                if (index == sizes.lastIndex) {
                    outPositions[index] = maxOf(currentOffset, totalSize - size)
                } else {
                    outPositions[index] = currentOffset
                    currentOffset += size
                }
            }
        }
    }
}
