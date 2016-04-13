package org.keynote.godtools.android.snuffy;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.keynote.godtools.android.utils.EventID;

import java.util.Set;

public class ParserUtils {
    private static Splitter SPLITTER_EVENTS = Splitter.on(',').omitEmptyStrings();

    public static Set<EventID> parseEvents(@Nullable final String raw, @NonNull final String namespace) {
        if (raw != null) {
            Set<EventID> eventIDs = Sets.newHashSet();

            for(String event : SPLITTER_EVENTS.split(raw)) {
                EventID eventID = new EventID();

                int indexOfColon = event.indexOf(":");

                if(indexOfColon == -1) {
                    eventID.setNamespace(namespace); /*if namespace isn't specified use the current package*/
                    eventID.setId(event);
                }
                else {
                    String id = event.substring(indexOfColon + 1);

                    if(Strings.isNullOrEmpty(id)) {
                        continue; /*if there's no id (text after the colon) this event is invalid*/
                    }
                    eventID.setNamespace(event.substring(0, indexOfColon));
                    eventID.setId(id);
                }

                eventIDs.add(eventID);
            }
            return ImmutableSet.copyOf(eventIDs);
        }
        return ImmutableSet.of();
    }
}
