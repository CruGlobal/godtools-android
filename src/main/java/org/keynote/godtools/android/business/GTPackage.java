package org.keynote.godtools.android.business;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Function;

import org.keynote.godtools.android.model.HomescreenLayout;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.Comparator;
import java.util.regex.Pattern;

@Root(name="package")
public class GTPackage implements Parcelable, Serializable {
    public static final String EVERYSTUDENT_PACKAGE_CODE = "everystudent";

    public static final String INVALID_CODE = "";

    public static final String STATUS_LIVE = "live";
    public static final String STATUS_DRAFT = "draft";

    public static final String DEFAULT_VERSION = "0";

    private static final Pattern PATTERN_VERSION = Pattern.compile("[0-9]+(?:\\.[0-9]+)*");

    public static final Function<GTPackage, String> FUNCTION_CODE = new Function<GTPackage, String>() {
        @Nullable
        @Override
        public String apply(@Nullable final GTPackage input) {
            return input != null ? input.code : null;
        }
    };
    private static final Comparator<GTPackage> COMPARATOR_VERSION = new VersionComparator();

    private String name;
    private String language;
    private String configFileName;

    @Attribute(name = "code", required = true)
    public String code;
    @Attribute(name = "version", required = true, empty=DEFAULT_VERSION)
    public String version;
    @Attribute(name = "status", required = true)
    public String status;


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

    @NonNull
    public String getVersion() {
        return version;
    }

    public void setVersion(@Nullable final String version) {
        this.version = version != null && PATTERN_VERSION.matcher(version).matches() ? version : DEFAULT_VERSION;
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
            final String lhs[] = lhsPackage.version.split("\\.");
            final String rhs[] = rhsPackage.version.split("\\.");

            for (int i = 0; i < lhs.length || i < rhs.length; i++) {
                final int lhsVal = i < lhs.length ? Integer.parseInt(lhs[i]) : 0;
                final int rhsVal = i < rhs.length ? Integer.parseInt(rhs[i]) : 0;
                final int result = lhsVal - rhsVal;
                if (result != 0) {
                    return result;
                }
            }

            return 0;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.language);
        dest.writeString(this.configFileName);
        dest.writeString(this.code);
        dest.writeString(this.version);
        dest.writeString(this.status);
        dest.writeByte(this.available ? (byte) 1 : (byte) 0);
    }

    protected GTPackage(Parcel in) {
        this.name = in.readString();
        this.language = in.readString();
        this.configFileName = in.readString();
        this.code = in.readString();
        this.version = in.readString();
        this.status = in.readString();
        this.available = in.readByte() != 0;
    }

    public static final Parcelable.Creator<GTPackage> CREATOR = new Parcelable.Creator<GTPackage>() {
        @Override
        public GTPackage createFromParcel(Parcel source) {
            return new GTPackage(source);
        }

        @Override
        public GTPackage[] newArray(int size) {
            return new GTPackage[size];
        }
    };
}
