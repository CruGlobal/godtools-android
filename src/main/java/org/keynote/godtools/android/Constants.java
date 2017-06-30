package org.keynote.godtools.android;

import android.net.Uri;

public final class Constants {
    public static final String ARG_PACKAGE = "package_code";
    public static final String ARG_STATUS = "package_status";
    public static final String ARG_LANGUAGE = "lang";
    public static final String ARG_MODAL_ID = "modal_id";

    // common extras
    public static final String EXTRA_TOOL = org.cru.godtools.base.Constants.EXTRA_TOOL;
    public static final String EXTRA_PRIMARY_LANGUAGE = org.cru.godtools.base.Constants.EXTRA_PRIMARY_LANGUAGE;
    public static final String EXTRA_PARALLEL_LANGUAGE = org.cru.godtools.base.Constants.EXTRA_PARALLEL_LANGUAGE;

    // SharedPreferences for this app
    public static final String PREFS_SETTINGS = "GodTools";
    public static final String PREF_PRIMARY_LANGUAGE = "languagePrimary";
    public static final String PREF_PARALLEL_LANGUAGE = "languageParallel";

    // Common base URI's
    public static final Uri MAILTO_SUPPORT = Uri.parse("mailto:support@godtoolsapp.com");
    public static final Uri URI_HELP = Uri.parse("http://www.godtoolsapp.com/");
    public static final Uri URI_PRIVACY = Uri.parse("https://www.cru.org/about/privacy.html");
    public static final Uri URI_TERMS_OF_USE = Uri.parse("http://www.godtoolsapp.com/");
    public static final String URI_SHARE_BASE = "http://www.knowgod.com/";
}
