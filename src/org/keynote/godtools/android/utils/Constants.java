package org.keynote.godtools.android.utils;

/**
 * Created by matthewfrederick on 5/5/15.
 */
public class Constants
{
    /**
     * Used as the key to type of background service for intent extras
     */
    public static final String TYPE = "TYPE";

    /**
     * Used as the key to sender of background service for intent extras
     */
    public static final String SENDER = "SENDER";

    /**
     * App preferences name
     */
    public static final String PREFS_NAME = "GodTools";


    /** constants used to specify the task that was run. */

    /**
     * Used to specify that the generic authentication task was run
     */
    public static final int AUTHENTICATE_GENERIC = 0;

    /** constants used to specify the sender of the intent to be run */

    /**
     * Intent was sent by the splash class
     */
    public static final int SPLASH = 0;

    /**
     * Intent was sent by the Main class
     */
    public static final int MAIN = 1;
}
