package org.cru.godtools.xml.model;

import org.jetbrains.annotations.Contract;
import org.xmlpull.v1.XmlPullParser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

public final class ImageGravity {
    static final int START = 1 << 0;
    public static final int END = 1 << 1;
    static final int TOP = 1 << 2;
    static final int BOTTOM = 1 << 3;
    private static final int CENTER_X = START | END;
    private static final int CENTER_Y = TOP | BOTTOM;
    static final int CENTER = CENTER_X | CENTER_Y;
    private static final int X_AXIS = START | END | CENTER_X;
    private static final int Y_AXIS = TOP | BOTTOM | CENTER_Y;

    @Nullable
    static Integer parse(@NonNull final XmlPullParser parser, @NonNull final String attribute,
                         @Nullable final Integer defValue) {
        return parse(parser.getAttributeValue(null, attribute), defValue);
    }

    @Nullable
    @Contract("_, !null -> !null")
    static Integer parse(@Nullable final String raw, @Nullable final Integer defValue) {
        if (raw != null) {
            try {
                int gravity = defValue != null ? defValue : CENTER;
                boolean seenX = false;
                boolean seenY = false;
                for (final String type : raw.split("\\s+")) {
                    switch (type) {
                        case "start":
                            if (seenX) {
                                throw new IllegalArgumentException("multiple X-Axis gravities in: " + raw);
                            }
                            gravity &= ~X_AXIS;
                            gravity |= START;
                            seenX = true;
                            break;
                        case "end":
                            if (seenX) {
                                throw new IllegalArgumentException("multiple X-Axis gravities in: " + raw);
                            }
                            gravity &= ~X_AXIS;
                            gravity |= END;
                            seenX = true;
                            break;
                        case "top":
                            if (seenY) {
                                throw new IllegalArgumentException("multiple Y-Axis gravities in: " + raw);
                            }
                            gravity &= ~Y_AXIS;
                            gravity |= TOP;
                            seenY = true;
                            break;
                        case "bottom":
                            if (seenY) {
                                throw new IllegalArgumentException("multiple Y-Axis gravities in: " + raw);
                            }
                            gravity &= ~Y_AXIS;
                            gravity |= BOTTOM;
                            seenY = true;
                            break;
                        case "center":
                            if (!seenX) {
                                gravity |= CENTER_X;
                            }
                            if (!seenY) {
                                gravity |= CENTER_Y;
                            }
                            break;
                        default:
                            // ignore unrecognized types
                    }
                }
                return gravity;
            } catch (final IllegalArgumentException e) {
                Timber.e(e, "error parsing ImageGravity");
            }
        }
        return defValue;
    }

    public static boolean isCenter(final int align) {
        return (align & CENTER) == CENTER;
    }

    /* X-Axis tests */

    public static boolean isCenterX(final int align) {
        return (align & X_AXIS) == CENTER_X;
    }

    public static boolean isStart(final int align) {
        return (align & X_AXIS) == START;
    }

    public static boolean isEnd(final int align) {
        return (align & X_AXIS) == END;
    }

    /* Y-Axis tests */

    public static boolean isCenterY(final int align) {
        return (align & Y_AXIS) == CENTER_Y;
    }

    public static boolean isTop(final int align) {
        return (align & Y_AXIS) == TOP;
    }

    public static boolean isBottom(final int align) {
        return (align & Y_AXIS) == BOTTOM;
    }
}
