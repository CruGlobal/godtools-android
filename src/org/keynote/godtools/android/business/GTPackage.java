package org.keynote.godtools.android.business;

import android.content.Context;

import org.keynote.godtools.android.dao.DBAdapter;

import java.util.List;

public class GTPackage {

    private String code;
    private String name;
    private int version;
    private String language;
    private String configFileName;
    private String status;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static List<GTPackage> getPackageByLanguage(Context context, String language){
        DBAdapter adapter = DBAdapter.getInstance(context);
        return adapter.getGTPackageByLanguage(language);
    }

    public long addToDatabase(Context context){
        DBAdapter adapter = DBAdapter.getInstance(context);
        return adapter.insertGTPackage(this);
    }
}
