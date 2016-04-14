package org.keynote.godtools.android.snuffy;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import org.keynote.godtools.android.event.GodToolsEvent;

import java.util.Set;

public class ParserUtils {
    private static Splitter SPLITTER_EVENTS = Splitter.on(',').omitEmptyStrings();

    public static Set<GodToolsEvent.EventID> parseEvents(@Nullable final String raw, @NonNull final String namespace) {
        if (raw != null) {
            ImmutableSet.Builder<GodToolsEvent.EventID> eventIDs = ImmutableSet.builder();

            for(String event : SPLITTER_EVENTS.split(raw)) {
                GodToolsEvent.EventID eventID;

                int indexOfColon = event.indexOf(":");

                if(indexOfColon == -1) {
                    /*if namespace isn't specified use the current package*/
                    eventID = new GodToolsEvent.EventID(namespace, event);
                }
                else {
                    String id = event.substring(indexOfColon + 1);

                    if(Strings.isNullOrEmpty(id)) {
                        continue; /*if there's no id (text after the colon) this event is invalid*/
                    }
                    eventID = new GodToolsEvent.EventID(event.substring(0, indexOfColon), id);
                }

                eventIDs.add(eventID);
            }
            return eventIDs.build();
        }
        return ImmutableSet.of();
    }
}
