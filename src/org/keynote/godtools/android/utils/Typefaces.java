package org.keynote.godtools.android.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.util.Hashtable;

/**
 * Created by ryancarlson on 4/17/14.
 *
 * Source: https://code.google.com/p/android/issues/detail?id=9904#c7
 * (Format altered for better style.)
 *
 */
public class Typefaces
{
	private static final String TAG = Typefaces.class.getSimpleName();

	private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

	public static Typeface get(Context c, String assetPath)
	{
		synchronized (cache)
		{
			if (!cache.containsKey(assetPath))
			{
				try
				{
					Typeface t = Typeface.createFromAsset(c.getAssets(),assetPath);
					cache.put(assetPath, t);
				}
				catch (Exception e)
				{
					Log.e(TAG, "Could not get typeface '" + assetPath + "' because " + e.getMessage());
					return null;
				}
			}
			return cache.get(assetPath);
		}
	}
}
