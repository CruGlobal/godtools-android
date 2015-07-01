package org.keynote.godtools.android.business;

import android.content.Context;

import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.model.HomescreenLayout;

import java.util.List;

public class GTPackage
{

    public static final String EVERYSTUDENT_PACKAGE_CODE = "everystudent";

    private long id;
    private String code;
    private String name;
    private double version;
    private String language;
    private String configFileName;
    private String status;
    private String icon;
    private HomescreenLayout layout;

    // in preview mode, all packages are shown; however, a package may not actually be available
    // to view.
    private boolean available;

    // set available to true as default
    public GTPackage()
    {
        this.setAvailable(true);
    }

    public static GTPackage getPackage(Context context, String code, String language, String status)
    {
        DBAdapter adapter = DBAdapter.getInstance(context);
        adapter.open();
        return adapter.getGTPackage(code, language, status);
    }

    public static List<GTPackage> getPackageByLanguage(Context context, String language)
    {
        DBAdapter adapter = DBAdapter.getInstance(context);
        adapter.open();
        return adapter.getGTPackageByLanguage(language);
    }

    public static List<GTPackage> getLivePackages(Context context, String language)
    {
        DBAdapter adapter = DBAdapter.getInstance(context);
        adapter.open();
        return adapter.getLiveGTPackage(language);
    }

    public static List<GTPackage> getDraftPackages(Context context, String language)
    {
        DBAdapter adapter = DBAdapter.getInstance(context);
        adapter.open();
        return adapter.getDraftGTPackage(language);
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public double getVersion()
    {
        return version;
    }

    public void setVersion(double version)
    {
        this.version = version;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getConfigFileName()
    {
        return configFileName;
    }

    public void setConfigFileName(String configFileName)
    {
        this.configFileName = configFileName;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    public HomescreenLayout getLayout()
    {
        return layout;
    }

    public void setLayout(HomescreenLayout layout)
    {
        this.layout = layout;
    }

    public boolean isAvailable()
    {
        return available;
    }

    public void setAvailable(boolean available)
    {
        this.available = available;
    }

    public long addToDatabase(Context context)
    {
        DBAdapter adapter = DBAdapter.getInstance(context);
        adapter.open();
        return adapter.insertGTPackage(this);
    }

    public void update(Context context)
    {
        DBAdapter adapter = DBAdapter.getInstance(context);
        adapter.open();
        adapter.upsertGTPackage(this);
    }
}
