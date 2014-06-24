package org.keynote.godtools.android.business;

import android.content.Context;

import org.keynote.godtools.android.dao.DBAdapter;

import java.util.List;

public class GTPackage {

    private String code;
    private String name;
    private double version;
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

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
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

    public static GTPackage getPackage(Context context, String code, String language){
        DBAdapter adapter = DBAdapter.getInstance(context);
        adapter.open();
        return adapter.getGTPackage(code, language);
    }

    public static List<GTPackage> getPackageByLanguage(Context context, String language){
        DBAdapter adapter = DBAdapter.getInstance(context);
        adapter.open();
        return adapter.getGTPackageByLanguage(language);
    }

    public long addToDatabase(Context context){
        DBAdapter adapter = DBAdapter.getInstance(context);
        adapter.open();
        return adapter.insertGTPackage(this);
    }
}
