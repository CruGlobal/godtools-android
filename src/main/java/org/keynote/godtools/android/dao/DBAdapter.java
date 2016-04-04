package org.keynote.godtools.android.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.async.AbstractAsyncDao;
import org.keynote.godtools.android.api.GSSubscriber;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.dao.DBContract.FollowupTable;
import org.keynote.godtools.android.dao.DBContract.GSSubscriberTable;
import org.keynote.godtools.android.dao.DBContract.GTPackageTable;
import org.keynote.godtools.android.model.Followup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DBAdapter extends AbstractAsyncDao {
    private static DBAdapter INSTANCE;

    private DBAdapter(@NonNull final Context context) {
        super(GodToolsDatabase.getInstance(context));

        registerType(GTPackage.class, GTPackageTable.TABLE_NAME, GTPackageTable.PROJECTION_ALL, new GTPackageMapper(),
                     GTPackageTable.SQL_WHERE_PRIMARY_KEY);
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
        } else if (obj instanceof GSSubscriber) {
            return getPrimaryKeyWhere(GSSubscriber.class, ((GSSubscriber) obj).getId());
        } else if (obj instanceof Followup) {
            final Followup followup = (Followup) obj;
            return getPrimaryKeyWhere(Followup.class, followup.getId(), followup.getContextId());
        }

        return super.getPrimaryKeyWhere(obj);
    }

    public long insertGTLanguage(GTLanguage gtLanguage) {
        ContentValues cv = new ContentValues();
        cv.put(DBContract.GTLanguageTable.COL_CODE, gtLanguage.getLanguageCode());
        cv.put(DBContract.GTLanguageTable.COL_IS_DOWNLOADED, gtLanguage.isDownloaded());
        cv.put(DBContract.GTLanguageTable.COL_IS_DRAFT, gtLanguage.isDraft());
        cv.put(DBContract.GTLanguageTable.COL_NAME, gtLanguage.getLanguageName());

        return getWritableDatabase().insert(DBContract.GTLanguageTable.TABLE_NAME, null, cv);
    }

    public List<GTLanguage> getAllLanguages()
    {
        return queryGTLanguage(null);
    }

    public GTLanguage getGTLanguage(String code)
    {
        String selection = String.format("%s = '%s'",
                DBContract.GTLanguageTable.COL_CODE, code);
        List<GTLanguage> languages = queryGTLanguage(selection);
        return languages.size() > 0 ? languages.get(0) : null;
    }

    public void updateGTLanguage(GTLanguage gtl)
    {
        ContentValues cv = new ContentValues();
        cv.put(DBContract.GTLanguageTable.COL_IS_DOWNLOADED, gtl.isDownloaded());
        cv.put(DBContract.GTLanguageTable.COL_IS_DRAFT, gtl.isDraft());
        cv.put(DBContract.GTLanguageTable.COL_NAME, gtl.getLanguageName());

        String where = String.format("%s = '%s'",
                DBContract.GTLanguageTable.COL_CODE, gtl.getLanguageCode());

        getWritableDatabase().update(DBContract.GTLanguageTable.TABLE_NAME, cv, where, null);
    }

    private List<GTLanguage> queryGTLanguage(String selection)
    {
        String[] projection = {DBContract.GTLanguageTable._ID,
                DBContract.GTLanguageTable.COL_CODE,
                DBContract.GTLanguageTable.COL_IS_DOWNLOADED,
                DBContract.GTLanguageTable.COL_IS_DRAFT,
                DBContract.GTLanguageTable.COL_NAME
        };

        Cursor cursor = getReadableDatabase()
                .query(DBContract.GTLanguageTable.TABLE_NAME, projection, selection, null, null, null, null);

        List<GTLanguage> listGTLanguages = new ArrayList<>();

        Locale current = Locale.getDefault();
        Locale.setDefault(new Locale("en"));

        while (cursor.moveToNext())
        {
            long id = cursor.getLong(cursor.getColumnIndex(DBContract.GTLanguageTable._ID));
            String code = cursor.getString(cursor.getColumnIndex(DBContract.GTLanguageTable.COL_CODE));
            boolean isDownloaded = cursor.getInt(cursor.getColumnIndex(DBContract.GTLanguageTable.COL_IS_DOWNLOADED)) > 0;
            boolean isDraft = cursor.getInt(cursor.getColumnIndex(DBContract.GTLanguageTable.COL_IS_DRAFT)) > 0;
            String name = cursor.getString(cursor.getColumnIndex(DBContract.GTLanguageTable.COL_NAME));

            GTLanguage gtl;

            if (name == null || name.isEmpty())
            {
                gtl = new GTLanguage(code);
            }
            else
            {
                gtl = new GTLanguage(code, name);
            }

            gtl.setId(id);
            gtl.setDownloaded(isDownloaded);
            gtl.setDraft(isDraft);

            listGTLanguages.add(gtl);
        }

        Locale.setDefault(current);

        cursor.close();

        return listGTLanguages;
    }
}
