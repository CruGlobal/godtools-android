package org.keynote.godtools.android.business;

import android.content.Context;

import org.apache.commons.lang3.text.WordUtils;
import org.ccci.gto.android.common.util.LocaleCompat;
import org.keynote.godtools.android.dao.DBAdapter;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class GTLanguage implements Serializable {

    public static final String KEY_PRIMARY = "languagePrimary";
    public static final String KEY_PARALLEL = "languageParallel";

    private long id;
    private String languageName;
    private String languageCode;
    private boolean downloaded;
    private boolean draft;
    private List<GTPackage> listPackages;

    public GTLanguage() {
    }

    public GTLanguage(String languageCode)
    {
        this.languageCode = languageCode;
        this.languageName = WordUtils.capitalize(
                LocaleCompat.forLanguageTag(languageCode).getDisplayName());
    }

    public GTLanguage(String languageCode, String languageName) {

        this.languageName = languageName;
        this.languageCode = languageCode;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLanguageName() {
        return languageName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
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

    public boolean isDraft()
    {
        return draft;
    }

    public void setDraft(boolean draft)
    {
        this.draft = draft;
    }

    public static GTLanguage getLanguage(Context context, String languageCode) {
        DBAdapter adapter = DBAdapter.getInstance(context);
        return adapter.getGTLanguage(languageCode);
    }

    public static List<GTLanguage> getAll(Context context) {
        DBAdapter adapter = DBAdapter.getInstance(context);

        return adapter.getAllLanguages();
    }

    /**
     * Gets all languages where the name is translated to the locale that is passed in
     * via @param locale.
     */
    public static List<GTLanguage> getAll(Context context, Locale locale) {
        DBAdapter adapter = DBAdapter.getInstance(context);

        List<GTLanguage> allLanguages = adapter.getAllLanguages();

        for(GTLanguage language : allLanguages)
        {
            String displayName = LocaleCompat.forLanguageTag(language.getLanguageCode()).getDisplayName(locale);
            language.setLanguageName(WordUtils.capitalize(displayName));
        }
        return allLanguages;
    }

    public void addToDatabase(Context context) {
        DBAdapter adapter = DBAdapter.getInstance(context);

        GTLanguage dbLanguage = adapter.getGTLanguage(languageCode);
        if (dbLanguage == null)
        {
            adapter.insertGTLanguage(this);
        }
        else
        {
            this.setDownloaded(dbLanguage.isDownloaded());
            adapter.updateGTLanguage(this);
        }
    }

    public void update(Context context) {
        DBAdapter adapter = DBAdapter.getInstance(context);
        adapter.updateGTLanguage(this);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof GTLanguage))
        {
            return false;
        }

        GTLanguage second = (GTLanguage) o;
        return this.getLanguageCode().equals(second.getLanguageCode());
    }
}
