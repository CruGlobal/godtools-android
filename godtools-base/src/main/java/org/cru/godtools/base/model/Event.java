package org.cru.godtools.base.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

public final class Event {
    @Immutable
    public static final class Id {
        public static final Id SUBSCRIBE_EVENT = new Id("followup", "subscribe");

        @NonNull
        private final String mNamespace;
        @NonNull
        private final String mName;

        Id(@NonNull final String namespace, @NonNull final String name) {
            mNamespace = namespace;
            mName = name;
        }

        @Override
        public boolean equals(@Nullable final Object obj) {
            return obj instanceof Id &&
                    mNamespace.equalsIgnoreCase(((Id) obj).mNamespace) &&
                    mName.equalsIgnoreCase(((Id) obj).mName);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(new String[] {mNamespace.toLowerCase(), mName.toLowerCase()});
        }

        public static Set<Id> parse(@NonNull final String namespace, @Nullable final String raw) {
            final ImmutableSet.Builder<Id> eventIds = ImmutableSet.builder();

            if (raw != null) {
                for (final String rawEvent : raw.split("\\s+")) {
                    final String[] components = rawEvent.split(":", 2);
                    if (components.length == 1) {
                        eventIds.add(new Id(namespace, components[0]));
                    } else {
                        eventIds.add(new Id(components[0], components[1]));
                    }
                }
            }
            return eventIds.build();
        }
    }
}
