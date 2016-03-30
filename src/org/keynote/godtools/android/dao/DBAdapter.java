package org.keynote.godtools.android.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Mapper;
import org.keynote.godtools.android.api.GSSubscriber;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DBAdapter extends AbstractDao {
    private static final Mapper<GTPackage> GT_PACKAGE_MAPPER = new GTPackageMapper();

    private static DBAdapter INSTANCE;

    private DBAdapter(@NonNull final Context context) {
        super(GodToolsDatabase.getInstance(context));
    }

    public static DBAdapter getInstance(@NonNull final Context context) {
        synchronized (DBAdapter.class) {
            if (INSTANCE == null) {
                INSTANCE = new DBAdapter(context);
            }
        }

        return INSTANCE;
    }

    @NonNull
    @Override
    protected String getTable(@NonNull final Class<?> clazz) {
        if (GTPackage.class.equals(clazz)) {
            return DBContract.GTPackageTable.TABLE_NAME;
        }

        return super.getTable(clazz);
    }

    @NonNull
    @Override
    public String[] getFullProjection(@NonNull final Class<?> clazz) {
        if (GTPackage.class.equals(clazz)) {
            return DBContract.GTPackageTable.PROJECTION_ALL;
        }

        return super.getFullProjection(clazz);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    protected <T> Mapper<T> getMapper(@NonNull Class<T> clazz) {
        if (GTPackage.class.equals(clazz)) {
            return (Mapper<T>) GT_PACKAGE_MAPPER;
        }

        return super.getMapper(clazz);
    }

    @NonNull
    @Override
    protected Expression getPrimaryKeyWhere(@NonNull final Class<?> clazz) {
        if (GTPackage.class.equals(clazz)) {
            return DBContract.GTPackageTable.SQL_WHERE_PRIMARY_KEY;
        }

        return super.getPrimaryKeyWhere(clazz);
    }

    @NonNull
    @Override
    protected Expression getPrimaryKeyWhere(@NonNull final Object obj) {
        if (obj instanceof GTPackage) {
            final GTPackage gtPackage = (GTPackage) obj;
            return getPrimaryKeyWhere(GTPackage.class, gtPackage.getLanguage(), gtPackage.getStatus(),
                                      gtPackage.getCode());
        }

        return super.getPrimaryKeyWhere(obj);
    }

    public long insertGTLanguage(GTLanguage gtLanguage)
    {
        ContentValues cv = new ContentValues();
        cv.put(DBContract.GTLanguageTable.COL_CODE, gtLanguage.getLanguageCode());
        cv.put(DBContract.GTLanguageTable.COL_IS_DOWNLOADED, gtLanguage.isDownloaded());
        cv.put(DBContract.GTLanguageTable.COL_IS_DRAFT, gtLanguage.isDraft());
        cv.put(DBContract.GTLanguageTable.COL_NAME, gtLanguage.getLanguageName());

        return getWritableDatabase().insert(DBContract.GTLanguageTable.TABLE_NAME, null, cv);
    }

    public long insertGSSubscriber(GSSubscriber gsSubscriber)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();

        ContentValues cv = new ContentValues();
        cv.put(DBContract.GSSubscriberTable.COL_ROUTE_ID, gsSubscriber.getRouteId());
        cv.put(DBContract.GSSubscriberTable.COL_LANGUAGE_CODE, gsSubscriber.getLanguageCode());
        cv.put(DBContract.GSSubscriberTable.COL_FIRST_NAME, gsSubscriber.getFirstName());
        cv.put(DBContract.GSSubscriberTable.COL_LAST_NAME, gsSubscriber.getLastName());
        cv.put(DBContract.GSSubscriberTable.COL_EMAIL, gsSubscriber.getEmail());
        cv.put(DBContract.GSSubscriberTable.COL_CREATED_TIMESTAMP, dateFormat.format(calendar.getTime()));

        return getWritableDatabase().insert(DBContract.GSSubscriberTable.TABLE_NAME, null, cv);
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
