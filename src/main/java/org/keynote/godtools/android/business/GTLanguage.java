package org.keynote.godtools.android.business;

import org.ccci.gto.android.common.util.LocaleCompat;
import org.keynote.godtools.android.utils.WordUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Root(name = "language")
public class GTLanguage implements Serializable {
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
