package org.keynote.godtools.android.business;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Function;

import org.keynote.godtools.android.R;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.core.Commit;

import java.io.Serializable;
import java.util.Comparator;
import java.util.regex.Pattern;

import static org.keynote.godtools.android.utils.Constants.FOUR_LAWS;
import static org.keynote.godtools.android.utils.Constants.KGP;
import static org.keynote.godtools.android.utils.Constants.SATISFIED;

@Element
public class GTPackage implements Parcelable, Serializable {

    public static final String TAG = "GTPackage";

    public static final String EVERYSTUDENT_PACKAGE_CODE = "everystudent";

    public static final String STATUS_LIVE = "live";
    public static final String STATUS_DRAFT = "draft";
    public static final String DEFAULT_VERSION = "0";
    public static final Function<GTPackage, String> FUNCTION_CODE = new Function<GTPackage, String>() {
        @Nullable
        @Override
        public String apply(@Nullable final GTPackage input) {
            return input != null ? input.code : null;
        }
    };
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
    private static final Pattern PATTERN_VERSION = Pattern.compile("[0-9]+(?:\\.[0-9]+)*");
    private static final Comparator<GTPackage> COMPARATOR_VERSION = new VersionComparator();
    @Attribute(name = "code", required = false)
    public String dummyCode;
    @Attribute(name = "package", required = false)
    public String code;
    @Attribute(name = "version", required = false)
    public String version;
    @Attribute(name = "status")
    public String status;
    @Attribute(name = "name", required = false)
    public String name;
    @Attribute(name = "language", required = false)
    public String language;
    @Attribute(name = "config", required = false)
    public String configFileName;
    @Attribute(name = "icon", required = false)
    public String icon;
    // in preview mode, all packages are shown; however, a package may not actually be available
    // to view.
    private boolean available;
    private boolean languageObj;

    // set available to true as default
    public GTPackage() {
        this.setAvailable(true);
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

    public static int getImageResourceByCode(String code) {
        if (KGP.equals(code)) {
            return R.drawable.bk_kgp;
        } else if (FOUR_LAWS.equals(code)) {
            return R.drawable.bk_waterfall;
        } else if (SATISFIED.equals(code)) {
            return R.drawable.bk_coast;
        } else {
            return R.drawable.place_holder_rich_media;
        }
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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Commit
    public void onCommit() {
        setVersion(version);
        if (dummyCode != null && !dummyCode.equalsIgnoreCase("")) {
            code = dummyCode;
        }
    }

    /**
     * @param other the package to compare versions against
     * @return an integer < 0 if the version of this package is less than the version of {@code other}, 0 if they are
     * equal, and > 0 if the version of this package is greater than the version of {@code other}.
     */
    public int compareVersionTo(@NonNull final GTPackage other) {
        return COMPARATOR_VERSION.compare(this, other);
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
}
