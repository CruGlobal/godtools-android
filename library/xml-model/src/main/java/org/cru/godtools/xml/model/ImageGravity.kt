package org.cru.godtools.xml.model

import org.xmlpull.v1.XmlPullParser
import timber.log.Timber

private const val BIT_START = 1 shl 0
private const val BIT_END = 1 shl 1
private const val BIT_TOP = 1 shl 2
private const val BIT_BOTTOM = 1 shl 3

private const val BIT_CENTER_X = BIT_START or BIT_END
private const val BIT_CENTER_Y = BIT_TOP or BIT_BOTTOM
private const val BIT_CENTER = BIT_CENTER_X or BIT_CENTER_Y

private const val MASK_X_AXIS = BIT_START or BIT_END or BIT_CENTER_X
private const val MASK_Y_AXIS = BIT_TOP or BIT_BOTTOM or BIT_CENTER_Y

inline class ImageGravity(internal val gravity: Int) {
    fun isCenter() = gravity and (MASK_X_AXIS or MASK_Y_AXIS) == BIT_CENTER
    fun isCenterX() = gravity and MASK_X_AXIS == BIT_CENTER_X
    fun isCenterY() = gravity and MASK_Y_AXIS == BIT_CENTER_Y
    fun isStart() = gravity and MASK_X_AXIS == BIT_START
    fun isEnd() = gravity and MASK_X_AXIS == BIT_END
    fun isTop() = gravity and MASK_Y_AXIS == BIT_TOP
    fun isBottom() = gravity and MASK_Y_AXIS == BIT_BOTTOM

    infix fun and(other: ImageGravity) = ImageGravity(gravity and other.gravity)
    infix fun or(other: ImageGravity) = ImageGravity(gravity or other.gravity)

    companion object {
        val START = ImageGravity(BIT_START)
        val END = ImageGravity(BIT_END)
        val TOP = ImageGravity(BIT_TOP)
        val BOTTOM = ImageGravity(BIT_BOTTOM)
        val CENTER = ImageGravity(BIT_CENTER)

        internal val NONE = ImageGravity(0)

        fun parse(raw: String?, defaultGravity: ImageGravity = CENTER): ImageGravity {
            if (raw == null) return defaultGravity

            try {
                var gravity = defaultGravity.gravity
                var seenX = false
                var seenY = false
                REGEX_SEQUENCE_SEPARATOR.split(raw).forEach {
                    when (it) {
                        "start" -> {
                            require(!seenX) { "multiple X-Axis gravities in: $raw" }
                            gravity = (gravity and MASK_X_AXIS.inv()) or BIT_START
                            seenX = true
                        }
                        "end" -> {
                            require(!seenX) { "multiple X-Axis gravities in: $raw" }
                            gravity = (gravity and MASK_X_AXIS.inv()) or BIT_END
                            seenX = true
                        }
                        "top" -> {
                            require(!seenY) { "multiple Y-Axis gravities in: $raw" }
                            gravity = (gravity and MASK_Y_AXIS.inv()) or BIT_TOP
                            seenY = true
                        }
                        "bottom" -> {
                            require(!seenY) { "multiple Y-Axis gravities in: $raw" }
                            gravity = (gravity and MASK_Y_AXIS.inv()) or BIT_BOTTOM
                            seenY = true
                        }
                        "center" -> {
                            if (!seenX) gravity = gravity or BIT_CENTER_X
                            if (!seenY) gravity = gravity or BIT_CENTER_Y
                        }
                    }
                }

                return ImageGravity(gravity)
            } catch (e: IllegalArgumentException) {
                Timber.e(e, "error parsing ImageGravity")
                return defaultGravity
            }
        }
    }
}

fun XmlPullParser.getAttributeValueAsImageGravity(name: String, defaultGravity: ImageGravity) =
    ImageGravity.parse(getAttributeValue(null, name), defaultGravity)

@JvmName("parse")
fun XmlPullParser.parseImageGravity(attribute: String, defaultGravity: Int) =
    getAttributeValueAsImageGravity(attribute, ImageGravity(defaultGravity)).gravity

const val CENTER = BIT_CENTER

fun isCenter(gravity: Int) = ImageGravity(gravity).isCenter()

/* X-Axis tests */
fun isCenterX(gravity: Int) = ImageGravity(gravity).isCenterX()
fun isStart(gravity: Int) = ImageGravity(gravity).isStart()
fun isEnd(gravity: Int) = ImageGravity(gravity).isEnd()

/* Y-Axis tests */
fun isCenterY(gravity: Int) = ImageGravity(gravity).isCenterY()
fun isTop(gravity: Int) = ImageGravity(gravity).isTop()
fun isBottom(gravity: Int) = ImageGravity(gravity).isBottom()
