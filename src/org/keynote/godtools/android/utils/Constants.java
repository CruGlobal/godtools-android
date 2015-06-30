package org.keynote.godtools.android.utils;

/**
 * Constants class used for all of application
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
    public static final String ENGLISH_DEFAULT = "en";
    public static final String EMPTY_STRING = "";


    public static final int REFERENCE_DEVICE_HEIGHT = 960;    // pixels on iPhone w/retina - including title bar
    public static final int REFERENCE_DEVICE_WIDTH = 640;    // pixels on iPhone w/retina - full width


    /**
     * App preferences name
     */
    public static final String PREFS_NAME = "GodTools";
    public static final String AUTH_GENERIC = "Authorization_Generic";
    public static final String AUTH_DRAFT = "Authorization_Draft";
    public static final String FIRST_LAUNCH = "firstLaunch";
    public static final String ACCESS_CODE = "access_code";
    public static final String TRANSLATOR_MODE = "TranslatorMode";
    public static final String NOTIFICATIONS = "Notifications";
    public static final String STATUS_CODE = "status_code";

    /**
     * package codes
     */
    public static final String KGP = "kgp";
    public static final String FOUR_LAWS = "fourlaws";
    public static final String SATISFIED = "satisfied";
    public static final String EVERY_STUDENT = "everystudent";

    public static final String REGISTRATION_ID = "registration_id";
    public static final String APP_VERSION = "appVersion";
    public static final String DEVICE_ID = "device_id";
    public static final String NOTIFICATIONS_ON = "notifications_on";

    /**
     * Intent extras
     */
    public static final String PAGE_LEFT = "PageLeft";
    public static final String PAGE_TOP = "PageTop";
    public static final String PAGE_WIDTH = "PageWidth";
    public static final String PAGE_HEIGHT = "PageHeight";
    public static final String PACKAGE_NAME = "PackageName";
    public static final String LANGUAGE_CODE = "LanguageCode";
    public static final String CONFIG_FILE_NAME = "ConfigFileName";
    public static final String STATUS = "Status";
    public static final String LANGUAGE_TYPE = "languageType";
    public static final String MAIN_LANGUAGE = "Main Language";
    public static final String PARALLEL_LANGUAGE = "Parallel Language";

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
     * Standard god tools result: disabled translator mode
     */
    public static final int RESULT_PREVIEW_MODE_DISABLED = 2345;

    public static final int REQUEST_PRIMARY = 1002;
    public static final int REQUEST_PARALLEL = 1003;

}
