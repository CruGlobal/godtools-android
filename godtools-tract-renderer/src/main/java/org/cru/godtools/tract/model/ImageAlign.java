package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.Contract;
import org.xmlpull.v1.XmlPullParser;

import io.fabric.sdk.android.Fabric;

final class ImageAlign {
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
    public static Integer parse(@NonNull final XmlPullParser parser, @NonNull final String attribute,
                                @Nullable final Integer defValue) {
        return parse(parser.getAttributeValue(null, attribute), defValue);
    }

    @Nullable
    @Contract("_, !null -> !null")
    static Integer parse(@Nullable final String raw, @Nullable final Integer defValue) {
        if (raw != null) {
            try {
                int scaleType = defValue != null ? defValue : CENTER;
                boolean seenX = false;
                boolean seenY = false;
                for (final String type : raw.split("\\s+")) {
                    switch (type) {
                        case "start":
                            if (seenX) {
                                throw new IllegalArgumentException("multiple X-Axis scale types in: " + raw);
                            }
                            scaleType &= ~X_AXIS;
                            scaleType |= START;
                            seenX = true;
                            break;
                        case "end":
                            if (seenX) {
                                throw new IllegalArgumentException("multiple X-Axis scale types in: " + raw);
                            }
                            scaleType &= ~X_AXIS;
                            scaleType |= END;
                            seenX = true;
                            break;
                        case "top":
                            if (seenY) {
                                throw new IllegalArgumentException("multiple Y-Axis scale types in: " + raw);
                            }
                            scaleType &= ~Y_AXIS;
                            scaleType |= TOP;
                            seenY = true;
                            break;
                        case "bottom":
                            if (seenY) {
                                throw new IllegalArgumentException("multiple Y-Axis scale types in: " + raw);
                            }
                            scaleType &= ~Y_AXIS;
                            scaleType |= BOTTOM;
                            seenY = true;
                            break;
                        case "center":
                            if (!seenX) {
                                scaleType |= CENTER_X;
                            }
                            if (!seenY) {
                                scaleType |= CENTER_Y;
                            }
                            break;
                        default:
                            // ignore unrecognized types
                    }
                }
                return scaleType;
            } catch (final IllegalArgumentException e) {
                if (Fabric.isInitialized()) {
                    Crashlytics.logException(e);
                }
            }
        }
        return defValue;
    }

    static boolean isCenter(final int align) {
        return (align & CENTER) == CENTER;
    }

    /* X-Axis tests */

    static boolean isCenterX(final int align) {
        return (align & X_AXIS) == CENTER_X;
    }

    static boolean isStart(final int align) {
        return (align & X_AXIS) == START;
    }

    static boolean isEnd(final int align) {
        return (align & X_AXIS) == END;
    }

    /* Y-Axis tests */

    static boolean isCenterY(final int align) {
        return (align & Y_AXIS) == CENTER_Y;
    }

    static boolean isTop(final int align) {
        return (align & Y_AXIS) == TOP;
    }

    static boolean isBottom(final int align) {
        return (align & Y_AXIS) == BOTTOM;
    }
}
