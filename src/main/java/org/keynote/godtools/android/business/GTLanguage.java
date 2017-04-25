package org.keynote.godtools.android.business;

import android.content.Context;

import org.ccci.gto.android.common.util.LocaleCompat;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.utils.WordUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.keynote.godtools.android.Constants.PREF_PARALLEL_LANGUAGE;
import static org.keynote.godtools.android.Constants.PREF_PRIMARY_LANGUAGE;

@Root(name = "language")
public class GTLanguage implements Serializable {
    public static final String KEY_PRIMARY = PREF_PRIMARY_LANGUAGE;
    public static final String KEY_PARALLEL = PREF_PARALLEL_LANGUAGE;

    @Attribute(name = "name", required = true)
    public String languageName;
    @Attribute(name = "code", required = true)
    private String languageCode;
    private boolean downloaded;
    @ElementList(name = "packages", entry = "package", required = true, type = GTPackage.class)
    private List<GTPackage> listPackages = new ArrayList<GTPackage>();

    private boolean isDraft;

    public GTLanguage() {
    }

    @Commit
    public void onCommit() {
        isDraft = isDraftCheck();
        addLanguageCodeToPackages();
    }

    private void addLanguageCodeToPackages() {
        for(GTPackage gtPackage : listPackages)
        {
            gtPackage.setLanguage(languageCode);
        }
    }

    public GTLanguage(String languageCode) {
        this.languageCode = languageCode;
        this.languageName = WordUtils.capitalize(
                LocaleCompat.forLanguageTag(languageCode).getDisplayName());
    }

    public GTLanguage(String languageCode, String languageName) {

        this.languageName = languageName;
        this.languageCode = languageCode;
    }

    /**
     * Gets all languages where the name is translated to the locale that is passed in
     * via @param locale.
     */
    public static List<GTLanguage> getAll(Context context, Locale locale) {
        DBAdapter adapter = DBAdapter.getInstance(context);

        List<GTLanguage> allLanguages = adapter.get(GTLanguage.class);

        for (GTLanguage language : allLanguages) {
            String displayName = LocaleCompat.forLanguageTag(language.getLanguageCode()).getDisplayName(locale);
            language.setLanguageName(WordUtils.capitalize(displayName));
        }
        return allLanguages;
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

    public List<GTPackage> getPackages() {
        return listPackages;
    }

    public void setPackages(List<GTPackage> listPackages) {
        this.listPackages = listPackages;
    }

    /*
    * If any package is live for the language the language will be live. It should
    * not be change back to draft by another package.*/
    public boolean isDraftCheck() {
        for (int i = 0; i < listPackages.size(); i++) {
            if ("live".equalsIgnoreCase(listPackages.get(i).status))
                return false;
        }
        return true;
    }

    public boolean isDraft()
    {
        return isDraft;
    }

    public void setDraft(boolean isDraft)
    {
        this.isDraft = isDraft;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof GTLanguage)) {
            return false;
        }

        GTLanguage second = (GTLanguage) o;
        return this.getLanguageCode().equals(second.getLanguageCode());
    }
}
