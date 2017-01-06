package org.keynote.godtools.android.snuffy;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import org.keynote.godtools.renderer.crureader.bo.GPage.Event.GodToolsEvent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
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

    @ColorInt
    @Nullable
    public static Integer safeParseColor(@Nullable final String rawColor, @Nullable final Integer defColor) {
        try {
            return Color.parseColor(rawColor);
        } catch (final Exception ignored) {
        }

        return defColor;
    }

    @Nullable
    public static Element getChildElementNamed(@NonNull final Element parent, @NonNull final String name) {
        Node node = parent.getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) node;
                if (el.getTagName().equalsIgnoreCase(name)) {
                    return el;
                }
            }
            node = node.getNextSibling();
        }
        return null;
    }

    @NonNull
    public static List<Element> getChildrenNamed(@NonNull final Element parent, @NonNull final String name) {
        List<Element> children = new ArrayList<>();
        Node node = parent.getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) node;
                if (el.getTagName().equalsIgnoreCase(name)) {
                    children.add(el);
                }
            }
            node = node.getNextSibling();
        }
        return children;
    }

    @NonNull
    public static String getTextContentImmediate(@NonNull final Element parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.TEXT_NODE) {
                return node.getNodeValue();
            }
            node = node.getNextSibling();
        }
        return "";
    }
}
