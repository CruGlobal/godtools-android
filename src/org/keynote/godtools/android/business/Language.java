package org.keynote.godtools.android.business;

/**
 * Created by john.jarder on 6/18/14.
 */
public class Language {

    public static final String MAIN_LANGUAGE_PREFKEY = "currMainLang";
    public static final String PARLLEL_LANGUAGE_PREFKEY = "currMainLang";

    private String languageName;
    private String languageCode;
    private boolean downloaded = false;

    public Language(){

    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }
}
