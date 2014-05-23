package org.keynote.godtools.android.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ryancarlson on 4/17/14.
 */
public class LanguagesNotSupportedByDefaultFont
{
	private static final Map<String, String> languageCodeSet = new HashMap<String, String>();

	static
	{
		languageCodeSet.put("th", "fonts/FreeSerif.ttf");
		languageCodeSet.put("ko", "fonts/UnGraphic.ttf");
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
