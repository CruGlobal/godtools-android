package org.cru.godtools.tract.model;

import android.support.annotation.Nullable;

import org.jetbrains.annotations.Contract;

final class ImageScaleType {
    public static final int START = 1 << 0;
    public static final int END = 1 << 1;
    public static final int TOP = 1 << 2;
    public static final int BOTTOM = 1 << 3;
    public static final int CENTER_X = START | END;
    public static final int CENTER_Y = TOP | BOTTOM;
    public static final int CENTER = CENTER_X | CENTER_Y;
    private static final int X_AXIS = START | END | CENTER_X;
    private static final int Y_AXIS = TOP | BOTTOM | CENTER_Y;

    @Nullable
    @Contract("_, !null -> !null")
    static Integer parse(@Nullable final String raw, @Nullable final Integer defValue) {
        if (raw != null) {
            int scaleType = defValue != null ? defValue : CENTER;
            boolean seenX = false;
            boolean seenY = false;
            for (final String type : raw.split("\\s+")) {
                switch (type) {
                    case "start":
                        scaleType &= ~X_AXIS;
                        scaleType |= START;
                        seenX = true;
                        break;
                    case "end":
                        scaleType &= ~X_AXIS;
                        scaleType |= END;
                        seenX = true;
                        break;
                    case "top":
                        scaleType &= ~Y_AXIS;
                        scaleType |= TOP;
                        seenY = true;
                        break;
                    case "bottom":
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
                }
            }
            return scaleType;
        }
        return defValue;
    }
}
