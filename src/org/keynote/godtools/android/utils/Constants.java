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

    public static final String KEY_NEW_LANGUAGE = "new_language";
    public static final String KEY_UPDATE_PRIMARY = "update_primary";
    public static final String KEY_UPDATE_PARALLEL = "update_parallel";
    public static final String KEY_PRIMARY = "primary";
    public static final String KEY_PARALLEL = "parallel";

    public static final String META = "meta";

    public static final String LANG_CODE = "lang_code";
    public static final String BACKGROUND_TASK_TAG = "task_tag";

    public static final String COUNT = "count";


    /**
     * App preferences name
     */
    public static final String PREFS_NAME = "GodTools";

    public static final String AUTH_CODE = "Authorization_Generic";

    /**
     * Standard god tools result: selected a primary language
     */
    public static final int RESULT_CHANGED_PRIMARY = 2003;

    /**
     * Standard god tools result: selected a parallel language
     */
    public static final int RESULT_CHANGED_PARALLEL = 2004;

    /**
     * Standard god tools result: selected a primary language that is not yet downloaded
     */
    public static final int RESULT_DOWNLOAD_PRIMARY = 2001;

    /**
     * Standard god tools result: selected a parallel language that is not yet downloaded
     */
    public static final int RESULT_DOWNLOAD_PARALLEL = 2002;

    /**
     * Standard god tools result: enabled translator mode
     */
    public static final int RESULT_PREVIEW_MODE_ENABLED = 1234;

    /**
     * Standard god tools result: disabled translator mode
     */
    public static final int RESULT_PREVIEW_MODE_DISABLED = 2345;
}
