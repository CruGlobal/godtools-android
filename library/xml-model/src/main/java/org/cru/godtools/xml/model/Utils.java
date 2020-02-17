package org.cru.godtools.xml.model;

import android.graphics.Color;
import android.net.Uri;

import org.jetbrains.annotations.Contract;
import org.xmlpull.v1.XmlPullParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

final class Utils {
    private static final Pattern COLOR_VALUE =
            Pattern.compile("^\\s*rgba\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9.]+)\\s*\\)\\s*$");

    @Nullable
    @Contract("_, !null -> !null")
    public static Boolean parseBoolean(@Nullable final String raw, @Nullable final Boolean defaultValue) {
        if (raw != null) {
            return Boolean.parseBoolean(raw);
        }

        return defaultValue;
    }

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

    @Nullable
    static ImageScaleType parseScaleType(@NonNull final XmlPullParser parser, @NonNull final String attribute,
                                         @Nullable final ImageScaleType defValue) {
        return ImageScaleType.parse(parser.getAttributeValue(null, attribute), defValue);
    }

    @Nullable
    static Uri parseUrl(@NonNull final XmlPullParser parser, @NonNull final String attribute,
                        @Nullable final Uri defValue) {
        return parseUrl(parser.getAttributeValue(null, attribute), defValue);
    }

    @Nullable
    static Uri parseUrl(@Nullable final String raw, @Nullable final Uri defValue) {
        if (raw != null) {
            final Uri uri = Uri.parse(raw);
            return uri.isAbsolute() ? uri : Uri.parse("http://" + raw);
        }
        return defValue;
    }
}
