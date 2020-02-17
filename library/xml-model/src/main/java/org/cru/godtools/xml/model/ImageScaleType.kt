package org.cru.godtools.xml.model

import org.jetbrains.annotations.Contract

enum class ImageScaleType {
    FIT, FILL, FILL_X, FILL_Y;

    companion object {
        @JvmStatic
        @Contract("_, !null -> !null")
        fun parse(value: String?, defValue: ImageScaleType?) = when (value) {
            "fit" -> FIT
            "fill" -> FILL
            "fill-y" -> FILL_Y
            "fill-x" -> FILL_X
            else -> defValue
        }
    }
}
