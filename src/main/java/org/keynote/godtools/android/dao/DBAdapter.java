package org.keynote.godtools.android.dao;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.async.AbstractAsyncDao;
import org.keynote.godtools.android.business.GSSubscriber;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.dao.DBContract.GSSubscriberTable;
import org.keynote.godtools.android.dao.DBContract.GTLanguageTable;
import org.keynote.godtools.android.dao.DBContract.GTPackageTable;
import org.keynote.godtools.android.db.GodToolsDao;

@Deprecated
public class DBAdapter extends AbstractAsyncDao {
    protected DBAdapter(@NonNull final SQLiteOpenHelper helper) {
        super(helper);

        registerType(GTPackage.class, GTPackageTable.TABLE_NAME, GTPackageTable.PROJECTION_ALL, new GTPackageMapper(),
                     GTPackageTable.SQL_WHERE_PRIMARY_KEY);
        registerType(GTLanguage.class, GTLanguageTable.TABLE_NAME, GTLanguageTable.PROJECTION_ALL,
                     new GTLanguageMapper(), GTLanguageTable.SQL_WHERE_PRIMARY_KEY);
        registerType(GSSubscriber.class, GSSubscriberTable.TABLE_NAME, GSSubscriberTable.PROJECTION_ALL,
                     new GSSubscriberMapper(), GSSubscriberTable.SQL_WHERE_PRIMARY_KEY);
    }

    @Deprecated
    public static DBAdapter getInstance(@NonNull final Context context) {
        return GodToolsDao.getInstance(context);
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
        }

        return super.getPrimaryKeyWhere(obj);
    }
}
