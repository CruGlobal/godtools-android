package org.keynote.godtools.android.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ryancarlson on 4/17/14.
 */
@SuppressWarnings("DefaultFileTemplate")
public class LanguagesNotSupportedByDefaultFont
{
    private static final Map<String, String> languageCodeSet = new HashMap<String, String>();

    static
    {
        languageCodeSet.put("ta", "fonts/FreeSerif.ttf");
        languageCodeSet.put("th", "fonts/FreeSerif.ttf");
        languageCodeSet.put("ko", "fonts/UnGraphic.ttf");
        languageCodeSet.put("bo", "fonts/Tibetan.ttf");
    }

    public static boolean contains(String languageCode)
    {
        return languageCodeSet.containsKey(languageCode);
    }

    public static String getPathToAlternateFont(String languageCode)
    {
        return languageCodeSet.get(languageCode);
    }
}
