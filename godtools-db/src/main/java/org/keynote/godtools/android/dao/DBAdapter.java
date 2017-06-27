package org.keynote.godtools.android.dao;

import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.async.AbstractAsyncDao;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.dao.DBContract.GTLanguageTable;
import org.keynote.godtools.android.dao.DBContract.GTPackageTable;

@Deprecated
public class DBAdapter extends AbstractAsyncDao {
    protected DBAdapter(@NonNull final SQLiteOpenHelper helper) {
        super(helper);

        registerType(GTPackage.class, GTPackageTable.TABLE_NAME, GTPackageTable.PROJECTION_ALL, new GTPackageMapper(),
                     GTPackageTable.SQL_WHERE_PRIMARY_KEY);
        registerType(GTLanguage.class, GTLanguageTable.TABLE_NAME, GTLanguageTable.PROJECTION_ALL,
                     new GTLanguageMapper(), GTLanguageTable.SQL_WHERE_PRIMARY_KEY);
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
        }

        return super.getPrimaryKeyWhere(obj);
    }
}
