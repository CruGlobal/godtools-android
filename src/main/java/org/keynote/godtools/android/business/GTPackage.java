package org.keynote.godtools.android.business;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Function;

import org.keynote.godtools.android.model.HomescreenLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;

public class GTPackage {
    public static final String EVERYSTUDENT_PACKAGE_CODE = "everystudent";

    public static final String STATUS_LIVE = "live";
    public static final String STATUS_DRAFT = "draft";

    public static final Function<GTPackage, String> FUNCTION_CODE = new Function<GTPackage, String>() {
        @Nullable
        @Override
        public String apply(@Nullable final GTPackage input) {
            return input != null ? input.code : null;
        }
    };
    private static final Comparator<GTPackage> COMPARATOR_VERSION = new VersionComparator();

    String code;
    private String name;
    double version;
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

    /**
     * @param other the package to compare versions against
     * @return an integer < 0 if the version of this package is less than the version of {@code other}, 0 if they are
     * equal, and > 0 if the version of this package is greater than the version of {@code other}.
     */
    public int compareVersionTo(@NonNull final GTPackage other) {
        return COMPARATOR_VERSION.compare(this, other);
    }

    static class VersionComparator implements Comparator<GTPackage> {
        @Override
        public int compare(final GTPackage lhsPackage, final GTPackage rhsPackage) {
            final BigDecimal lhs = BigDecimal.valueOf(lhsPackage.version);
            final BigDecimal rhs = BigDecimal.valueOf(rhsPackage.version);

            //compare int parts first
            int result = lhs.intValue() - rhs.intValue();
            if (result != 0) {
                return result;
            }

            // otherwise compare decimal part as an integer
            // see: http://stackoverflow.com/questions/10383392/extract-number-decimal-in-bigdecimal
            return lhs.subtract(lhs.setScale(0, RoundingMode.FLOOR)).movePointRight(lhs.scale()).intValue() -
                    rhs.subtract(rhs.setScale(0, RoundingMode.FLOOR)).movePointRight(rhs.scale()).intValue();
        }
    }
}
