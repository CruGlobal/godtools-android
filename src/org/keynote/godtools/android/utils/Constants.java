package org.keynote.godtools.android.utils;

/**
 * Class containing constants used through out the GodTools Application
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
    public static final String KEY_DRAFT = "draft";

    public static final String META = "meta";

    public static final String LANG_CODE = "lang_code";
    public static final String BACKGROUND_TASK_TAG = "task_tag";

    public static final String COUNT = "count";


    /**
     * Intent Constants
     */
    public static final String PACKAGE_NAME = "PackageName";
    public static final String LANGUAGE_CODE = "LanguageCode";
    public static final String CONFIG_FILE_NAME = "ConfigFileName";
    public static final String STATUS = "Status";
    public static final String PAGE_LEFT = "PageLeft";
    public static final String PAGE_TOP = "PageTop";
    public static final String PAGE_WIDTH = "PageWidth";
    public static final String PAGE_HEIGHT = "PageHeight";
    public static final String ALLOW_FLIP = "AllowFlip";
    public static final String PACKAGE_TITLE = "PackageTitle";

    /**
     * App preferences
     */
    public static final String PREFS_NAME = "GodTools";
    public static final String AUTH_CODE = "Authorization_Generic";
    public static final String FIRST_LAUNCH = "firstLaunch";
    public static final String LANGUAGE_PARALLEL = "languageParallel";
    public static final String CURRENT_PAGE = "currPage";
    public static final String CURRENT_LANGUAGE_CODE = "currLanguageCode";

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

    public static final int REQUEST_SETTINGS = 1001;
    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static final String SENDER_ID = "237513440670";

    public static final int REFERENCE_DEVICE_HEIGHT = 960;    // pixels on iPhone w/retina - including title bar
    public static final int REFERENCE_DEVICE_WIDTH = 640;    // pixels on iPhone w/retina - full width

    public static final String AUTH_DRAFT = "Authorization_Draft";

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

    public static final String ACCESS_CODE = "access_code";
    public static final String TRANSLATOR_MODE = "TranslatorMode";
    public static final String TRANSLATOR_MODE_EXPIRED = "translator_mode_expired";
    public static final String STATUS_CODE = "status_code";

    public static final String EMPTY_STRING = "";
    public static final String ENGLISH_DEFAULT = "en";

    public static final String EN_HEARTBEAT = "en_heartbeat";
    public static final String ET_HEARTBEAT = "et_heartbeat";
}
