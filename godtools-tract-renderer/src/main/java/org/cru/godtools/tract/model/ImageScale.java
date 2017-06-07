package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jetbrains.annotations.Contract;
import org.xmlpull.v1.XmlPullParser;

enum ImageScale {
    FIT, FILL, FILL_X, FILL_Y;

    @Nullable
    public static ImageScale parse(@NonNull final XmlPullParser parser, @NonNull final String attribute,
                                   @Nullable final ImageScale defValue) {
        return parse(parser.getAttributeValue(null, attribute), defValue);
    }

    @Nullable
    @Contract("_, !null -> !null")
    static ImageScale parse(@Nullable final String value, @Nullable final ImageScale defValue) {
        if (value != null) {
            try {
                switch (value) {
                    case "fill-y":
                        return FILL_Y;
                    case "fill-x":
                        return FILL_X;
                    default:
                        return ImageScale.valueOf(value.toUpperCase());
                }
            } catch (final Exception ignored) {
            }
        }
        return defValue;
    }
}
