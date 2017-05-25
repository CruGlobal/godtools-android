package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jetbrains.annotations.Contract;
import org.xmlpull.v1.XmlPullParser;

enum Align {
    CENTER, START, END, TOP, BOTTOM, REPEAT;

    @Nullable
    public static Align parseAlign(@NonNull final XmlPullParser parser, @NonNull final String attribute,
                                   @Nullable final Align defValue) {
        return parseAlign(parser.getAttributeValue(null, attribute), defValue);
    }

    @Nullable
    @Contract("_, !null -> !null")
    static Align parseAlign(@Nullable final String value, @Nullable final Align defValue) {
        if (value != null) {
            try {
                return Align.valueOf(value.toUpperCase());
            } catch (final Exception ignored) {
            }
        }
        return defValue;
    }
}
