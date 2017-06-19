package org.cru.godtools.base.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Event {
    @NonNull
    public final Id id;
    @NonNull
    private final Map<String, String> mFields;

    Event(@NonNull final Builder builder) {
        id = checkNotNull(builder.mId);
        mFields = ImmutableMap.copyOf(builder.mFields);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Immutable
    public static final class Id {
        public static final Id FOLLOWUP_EVENT = new Id("followup", "send");

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

        public static Set<Id> parse(@NonNull final String defaultNamespace, @Nullable final String raw) {
            final ImmutableSet.Builder<Id> eventIds = ImmutableSet.builder();

            if (raw != null) {
                for (final String rawEvent : raw.split("\\s+")) {
                    final String[] components = rawEvent.split(":", 2);
                    if (components.length == 1) {
                        eventIds.add(new Id(defaultNamespace, components[0]));
                    } else {
                        eventIds.add(new Id(components[0], components[1]));
                    }
                }
            }
            return eventIds.build();
        }
    }

    public static class Builder {
        @Nullable
        Id mId;

        @NonNull
        final Map<String, String> mFields = new HashMap<>();

        Builder() {}

        public Builder id(@NonNull final Id id) {
            mId = id;
            return this;
        }

        public Builder field(@NonNull final String name, @NonNull final String value) {
            mFields.put(name, value);
            return this;
        }

        public Event build() {
            return new Event(this);
        }
    }
}
