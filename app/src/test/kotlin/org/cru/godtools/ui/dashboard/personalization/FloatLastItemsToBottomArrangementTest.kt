package org.cru.godtools.ui.dashboard.personalization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.Density
import kotlin.test.Test
import kotlin.test.assertContentEquals

class FloatLastItemsToBottomArrangementTest {
    private val density = Density(1f)

    private fun Arrangement.Vertical.testArrange(totalSize: Int, vararg sizes: Int): IntArray {
        val outPositions = IntArray(sizes.size)
        with(density) { arrange(totalSize, sizes, outPositions) }
        return outPositions
    }

    // region numToFloat = 0
    @Test
    fun `numToFloat=0 - items positioned sequentially from top`() {
        val arrangement = FloatLastItemsToBottomArrangement(numToFloat = 0)
        assertContentEquals(intArrayOf(0, 100, 200), arrangement.testArrange(500, 100, 100, 100))
    }
    // endregion numToFloat = 0

    // region numToFloat = 1
    @Test
    fun `numToFloat=1 - last item floats to bottom when content fits`() {
        val arrangement = FloatLastItemsToBottomArrangement(numToFloat = 1)
        assertContentEquals(intArrayOf(0, 100, 400), arrangement.testArrange(500, 100, 100, 100))
    }

    @Test
    fun `numToFloat=1 - no floating when content overflows container`() {
        val arrangement = FloatLastItemsToBottomArrangement(numToFloat = 1)
        assertContentEquals(intArrayOf(0, 100, 200), arrangement.testArrange(200, 100, 100, 100))
    }

    @Test
    fun `numToFloat=1 - single item floats to bottom`() {
        val arrangement = FloatLastItemsToBottomArrangement(numToFloat = 1)
        assertContentEquals(intArrayOf(400), arrangement.testArrange(500, 100))
    }
    // endregion numToFloat = 1

    // region numToFloat = 2
    @Test
    fun `numToFloat=2 - last 2 items float to bottom when content fits`() {
        val arrangement = FloatLastItemsToBottomArrangement(numToFloat = 2)
        assertContentEquals(intArrayOf(0, 300, 400), arrangement.testArrange(500, 100, 100, 100))
    }

    @Test
    fun `numToFloat=2 - no floating when content exactly fills container`() {
        val arrangement = FloatLastItemsToBottomArrangement(numToFloat = 2)
        assertContentEquals(intArrayOf(0, 100, 200), arrangement.testArrange(300, 100, 100, 100))
    }
    // endregion numToFloat = 2

    // region numToFloat exceeds item count
    @Test
    fun `numToFloat exceeds item count - all items float to bottom`() {
        val arrangement = FloatLastItemsToBottomArrangement(numToFloat = 5)
        assertContentEquals(intArrayOf(200, 300, 400), arrangement.testArrange(500, 100, 100, 100))
    }
    // endregion numToFloat exceeds item count

    // region edge cases
    @Test
    fun `empty item list`() {
        val arrangement = FloatLastItemsToBottomArrangement(numToFloat = 1)
        assertContentEquals(intArrayOf(), arrangement.testArrange(500))
    }

    @Test
    fun `variable item sizes - floating item positioned correctly`() {
        val arrangement = FloatLastItemsToBottomArrangement(numToFloat = 1)
        assertContentEquals(intArrayOf(0, 50, 350), arrangement.testArrange(500, 50, 200, 150))
    }
    // endregion edge cases
}
