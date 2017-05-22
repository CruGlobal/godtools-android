package org.cru.godtools.tract.model;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jetbrains.annotations.Contract;
import org.xmlpull.v1.XmlPullParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Utils {
    private static final Pattern COLOR_VALUE =
            Pattern.compile("^\\s*rgba\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9.]+)\\s*\\)\\s*$");

    @Nullable
    @ColorInt
    static Integer parseColor(@NonNull final XmlPullParser parser, @NonNull final String attribute,
                              @Nullable @ColorInt final Integer defValue) {
        return parseColor(parser.getAttributeValue(null, attribute), defValue);
    }

    @Nullable
    @ColorInt
    @Contract("_, !null -> !null")
    static Integer parseColor(@Nullable final String rgba, @Nullable @ColorInt final Integer defValue) {
        if (rgba != null) {
            final Matcher matcher = COLOR_VALUE.matcher(rgba);
            if (matcher.matches()) {
                try {
                    return Color.argb(
                            (int) (Double.parseDouble(matcher.group(4)) * 255),
                            Integer.parseInt(matcher.group(1)),
                            Integer.parseInt(matcher.group(2)),
                            Integer.parseInt(matcher.group(3))
                    );
                } catch (final Exception ignored) {
                }
            }
        }
        return defValue;
    }
}
