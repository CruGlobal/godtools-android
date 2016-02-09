package org.keynote.godtools.android.business;

import android.content.Context;
import static org.ccci.gto.android.common.db.Expression.bind;
import static org.keynote.godtools.android.dao.DBContract.GTPackageTable.FIELD_LANGUAGE;
import static org.keynote.godtools.android.dao.DBContract.GTPackageTable.FIELD_STATUS;

import android.support.annotation.Nullable;

import com.google.common.base.Function;

import org.keynote.godtools.android.dao.DBAdapter;
import org.ccci.gto.android.common.db.Expression;
import org.keynote.godtools.android.model.HomescreenLayout;

import java.util.List;

public class GTPackage {
    public static final String EVERYSTUDENT_PACKAGE_CODE = "everystudent";

    public static final String STATUS_LIVE = "live";
    public static final String STATUS_DRAFT = "draft";

    // common SQL queries
    public static final Expression SQL_WHERE_DRAFT_BY_LANGUAGE =
            FIELD_LANGUAGE.eq(bind()).and(FIELD_STATUS.eq(STATUS_DRAFT));

    public static final Function<GTPackage, String> FUNCTION_CODE = new Function<GTPackage, String>() {
        @Nullable
        @Override
        public String apply(@Nullable final GTPackage input) {
            return input != null ? input.code : null;
        }
    };

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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
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

    public static GTPackage getPackage(Context context, String code, String language, String status){
        DBAdapter adapter = DBAdapter.getInstance(context);
        return adapter.getGTPackage(code, language, status);
    }

    public static List<GTPackage> getPackageByLanguage(Context context, String language){
        DBAdapter adapter = DBAdapter.getInstance(context);
        return adapter.getGTPackageByLanguage(language);
    }

    public static List<GTPackage> getDraftPackages(Context context, String language) {
        DBAdapter adapter = DBAdapter.getInstance(context);
        return adapter.getDraftGTPackage(language);
    }

    public long addToDatabase(Context context){
        DBAdapter adapter = DBAdapter.getInstance(context);
        return adapter.insert(this);
    }

    public void update(Context context) {
        DBAdapter adapter = DBAdapter.getInstance(context);
        adapter.upsertGTPackage(this);
    }
}
