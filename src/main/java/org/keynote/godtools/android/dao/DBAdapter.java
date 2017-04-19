package org.keynote.godtools.android.dao;

import android.content.Context;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.async.AbstractAsyncDao;
import org.keynote.godtools.android.business.GSSubscriber;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.dao.DBContract.FollowupTable;
import org.keynote.godtools.android.dao.DBContract.GSSubscriberTable;
import org.keynote.godtools.android.dao.DBContract.GTLanguageTable;
import org.keynote.godtools.android.dao.DBContract.GTPackageTable;
import org.keynote.godtools.android.db.GodToolsDatabase;
import org.keynote.godtools.android.model.Followup;

public class DBAdapter extends AbstractAsyncDao {
    private static DBAdapter INSTANCE;

    private DBAdapter(@NonNull final Context context) {
        super(GodToolsDatabase.getInstance(context));

        registerType(GTPackage.class, GTPackageTable.TABLE_NAME, GTPackageTable.PROJECTION_ALL, new GTPackageMapper(),
                     GTPackageTable.SQL_WHERE_PRIMARY_KEY);
        registerType(GTLanguage.class, GTLanguageTable.TABLE_NAME, GTLanguageTable.PROJECTION_ALL,
                     new GTLanguageMapper(), GTLanguageTable.SQL_WHERE_PRIMARY_KEY);
        registerType(GSSubscriber.class, GSSubscriberTable.TABLE_NAME, GSSubscriberTable.PROJECTION_ALL,
                     new GSSubscriberMapper(), GSSubscriberTable.SQL_WHERE_PRIMARY_KEY);
        registerType(Followup.class, FollowupTable.TABLE_NAME, FollowupTable.PROJECTION_ALL, new FollowupMapper(),
                     FollowupTable.SQL_WHERE_PRIMARY_KEY);
    }

    public static DBAdapter getInstance(@NonNull final Context context) {
        synchronized (DBAdapter.class) {
            if (INSTANCE == null) {
                INSTANCE = new DBAdapter(context.getApplicationContext());
            }
        }

        return INSTANCE;
    }

    @NonNull
    @Override
    protected Expression getPrimaryKeyWhere(@NonNull final Object obj) {
        if (obj instanceof GTPackage) {
            final GTPackage gtPackage = (GTPackage) obj;
            return getPrimaryKeyWhere(GTPackage.class, gtPackage.getLanguage(), gtPackage.getStatus(),
                                      gtPackage.getCode());
        } else if (obj instanceof GTLanguage) {
            return getPrimaryKeyWhere(GTLanguage.class, ((GTLanguage) obj).getLanguageCode());
        } else if (obj instanceof GSSubscriber) {
            return getPrimaryKeyWhere(GSSubscriber.class, ((GSSubscriber) obj).getId());
        } else if (obj instanceof Followup) {
            final Followup followup = (Followup) obj;
            return getPrimaryKeyWhere(Followup.class, followup.getId(), followup.getContextId());
        }

        return super.getPrimaryKeyWhere(obj);
    }
}
