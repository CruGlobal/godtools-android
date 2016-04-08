package org.keynote.godtools.android.snuffy;

import android.support.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class ParserUtils {
    private static Splitter SPLITTER_EVENTS = Splitter.on(',').omitEmptyStrings();

    public static Set<String> parseEvents(@Nullable final String raw) {
        if (raw != null) {
            return ImmutableSet.copyOf(SPLITTER_EVENTS.split(raw));
        }
        return ImmutableSet.of();
    }
}
