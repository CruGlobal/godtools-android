package org.cru.godtools.base.tool.model

import kotlin.math.abs
import org.cru.godtools.tool.model.Dimension

private const val MAX_PRECISION_DELTA = 0.000001f
internal fun Dimension.Percent.compareTo(other: Float) = when {
    abs(value - other) < MAX_PRECISION_DELTA -> 0
    else -> value.compareTo(other)
}
