package org.cru.godtools.articles.aem.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "aemImports")
public class AemImport {
    @NonNull
    @PrimaryKey
    public final Uri uri;

    @NonNull
    public Date lastProcessed = new Date(0);

    public AemImport(@NonNull final Uri uri) {
        this.uri = uri;
    }
}
