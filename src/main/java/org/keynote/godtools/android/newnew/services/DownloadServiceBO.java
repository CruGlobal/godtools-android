package org.keynote.godtools.android.newnew.services;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rmatt on 3/14/2017.
 */

public class DownloadServiceBO implements Parcelable {



    String url;
    String filePath;
    String tag;
    String authorization;
    String langCode;

    public DownloadServiceBO(String url, String filePath, String tag, String authorization, String langCode) {
        this.url = url;
        this.filePath = filePath;
        this.tag = tag;
        this.authorization = authorization;
        this.langCode = langCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.filePath);
        dest.writeString(this.tag);
        dest.writeString(this.authorization);
        dest.writeString(this.langCode);
    }

    protected DownloadServiceBO(Parcel in) {
        this.url = in.readString();
        this.filePath = in.readString();
        this.tag = in.readString();
        this.authorization = in.readString();
        this.langCode = in.readString();
    }

    public static final Parcelable.Creator<DownloadServiceBO> CREATOR = new Parcelable.Creator<DownloadServiceBO>() {
        @Override
        public DownloadServiceBO createFromParcel(Parcel source) {
            return new DownloadServiceBO(source);
        }

        @Override
        public DownloadServiceBO[] newArray(int size) {
            return new DownloadServiceBO[size];
        }
    };
}
