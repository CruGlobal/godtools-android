package org.keynote.godtools.android.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.db.Mapper;
import org.ccci.gto.android.common.db.Query;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;

import java.util.ArrayList;
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

    public long insertGTLanguage(GTLanguage gtLanguage)
    {
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

    public GTPackage getGTPackage(String code, String language, String status)
    {
        String selection = String.format("%s = '%s' AND %s = '%s' AND %s = '%s'",
                DBContract.GTPackageTable.COL_CODE, code,
                DBContract.GTPackageTable.COL_LANGUAGE, language,
                DBContract.GTPackageTable.COL_STATUS, status);
        List<GTPackage> packages = queryGTPackage(selection);
        return packages.size() > 0 ? packages.get(0) : null;
    }

    public GTLanguage getGTLanguage(String code)
    {
        String selection = String.format("%s = '%s'",
                DBContract.GTLanguageTable.COL_CODE, code);
        List<GTLanguage> languages = queryGTLanguage(selection);
        return languages.size() > 0 ? languages.get(0) : null;
    }

    public List<GTPackage> getGTPackageByLanguage(String language)
    {
        String selection = String.format("%s = '%s'", DBContract.GTPackageTable.COL_LANGUAGE, language);
        return queryGTPackage(selection);
    }

    public List<GTPackage> getDraftGTPackage(String language)
    {
        String selection = String.format("%s = '%s' AND %s = 'draft'",
                DBContract.GTPackageTable.COL_LANGUAGE, language,
                DBContract.GTPackageTable.COL_STATUS);

        return queryGTPackage(selection);
    }

    public void deletePackages(String language, String status)
    {
        String selection = String.format("%s = '%s' AND %s = '%s'",
                DBContract.GTPackageTable.COL_LANGUAGE, language,
                DBContract.GTPackageTable.COL_STATUS, status);

        getWritableDatabase().delete(DBContract.GTPackageTable.TABLE_NAME, selection, null);
    }

    public void upsertGTPackage(GTPackage gtp)
    {
        ContentValues cv = new ContentValues();

        cv.put(DBContract.GTPackageTable.COL_NAME, gtp.getName());
        cv.put(DBContract.GTPackageTable.COL_LANGUAGE, gtp.getLanguage());
        cv.put(DBContract.GTPackageTable.COL_CODE, gtp.getCode());
        cv.put(DBContract.GTPackageTable.COL_STATUS, gtp.getStatus());
        cv.put(DBContract.GTPackageTable.COL_CONFIG_FILE_NAME, gtp.getConfigFileName());
        cv.put(DBContract.GTPackageTable.COL_ICON, gtp.getIcon());
        cv.put(DBContract.GTPackageTable.COL_VERSION, gtp.getVersion());

        String where = String.format("%s = '%s' AND %s = '%s' AND %s = '%s'",
                DBContract.GTPackageTable.COL_CODE, gtp.getCode(),
                DBContract.GTPackageTable.COL_LANGUAGE, gtp.getLanguage(),
                DBContract.GTPackageTable.COL_STATUS, gtp.getStatus());

        int numberOfAffectedRows = getWritableDatabase().update(DBContract.GTPackageTable.TABLE_NAME, cv, where, null);

        if (numberOfAffectedRows == 0)
        {
            getWritableDatabase().insert(DBContract.GTPackageTable.TABLE_NAME, null, cv);
        }
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

    private List<GTPackage> queryGTPackage(String selection)
    {
        return get(Query.select(GTPackage.class).where(selection).orderBy(DBContract.GTPackageTable._ID));
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

        List<GTLanguage> listGTLanguages = new ArrayList<GTLanguage>();

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
