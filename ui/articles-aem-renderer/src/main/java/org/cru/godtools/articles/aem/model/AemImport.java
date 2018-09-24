package org.cru.godtools.articles.aem.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.Date;

import static org.ccci.gto.android.common.base.TimeConstants.DAY_IN_MS;

@Entity(tableName = "aemImports")
public class AemImport {
    private static final long STALE_AGE = DAY_IN_MS;

    @NonNull
    @PrimaryKey
    public final Uri uri;

    @NonNull
    public Date lastProcessed = new Date(0);

    public AemImport(@NonNull final Uri uri) {
        this.uri = uri;
    }

    public boolean isStale() {
        return lastProcessed.before(new Date(System.currentTimeMillis() - STALE_AGE));
    }
}
