package org.keynote.godtools.android;

import android.support.v7.app.ActionBarActivity;

public class BaseActionBarActivity extends ActionBarActivity {

    public static final String PREFS_NAME = "GodTools";

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
