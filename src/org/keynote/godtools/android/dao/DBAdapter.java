package org.keynote.godtools.android.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;

import java.util.ArrayList;
import java.util.List;

public class DBAdapter {

    private static DBAdapter instance;
    private DBHelper helper;
    private SQLiteDatabase db;

    private DBAdapter(Context context) {
        helper = new DBHelper(context);
    }

    public static DBAdapter getInstance(Context context) {
        if (instance == null)
            instance = new DBAdapter(context);

        return instance;
    }

    public void open() {
        db = helper.getWritableDatabase();
    }

    public void close() {
        helper.close();
    }


    public long insertGTPackage(GTPackage gtPackage) {
        ContentValues cv = new ContentValues();
        cv.put(DBContract.GTPackageTable.COL_CODE, gtPackage.getCode());
        cv.put(DBContract.GTPackageTable.COL_NAME, gtPackage.getName());
        cv.put(DBContract.GTPackageTable.COL_LANGUAGE, gtPackage.getLanguage());
        cv.put(DBContract.GTPackageTable.COL_VERSION, gtPackage.getVersion());
        cv.put(DBContract.GTPackageTable.COL_CONFIG_FILE_NAME, gtPackage.getConfigFileName());
        cv.put(DBContract.GTPackageTable.COL_STATUS, gtPackage.getStatus());

        return db.insert(DBContract.GTPackageTable.TABLE_NAME, null, cv);
    }

    public long insertGTLanguage(GTLanguage gtLanguage){
        ContentValues cv = new ContentValues();
        cv.put(DBContract.GTLanguageTable.COL_CODE, gtLanguage.getLanguageCode());
        cv.put(DBContract.GTLanguageTable.COL_IS_DOWNLOADED, gtLanguage.isDownloaded());

        return db.insert(DBContract.GTLanguageTable.TABLE_NAME, null, cv);
    }

    public List<GTLanguage> getAllLanguages(){
        return queryGTLanguage(null);
    }

    public GTPackage getGTPackage(String code, String language){
        String selection = String.format("%s = '%s' AND %s = '%s'",
                                        DBContract.GTPackageTable.COL_CODE, code,
                                        DBContract.GTPackageTable.COL_LANGUAGE, language);
        List<GTPackage> packages = queryGTPackage(selection);
        return packages.size() > 0 ? packages.get(0) : null;
    }

    public GTLanguage getGTLanguage(String code){
        String selection = String.format("%s = '%s'",
                                        DBContract.GTLanguageTable.COL_CODE, code);
        List<GTLanguage> languages = queryGTLanguage(selection);
        return languages.size() > 0 ? languages.get(0) : null;
    }

    public List<GTPackage> getGTPackageByLanguage(String language){
        String selection = String.format("%s = '%s'", DBContract.GTPackageTable.COL_LANGUAGE, language);
        return queryGTPackage(selection);
    }

    public void updateGTPackage(GTPackage gtp){
        ContentValues cv = new ContentValues();

        if (gtp.getVersion() > 0) {
            cv.put(DBContract.GTPackageTable.COL_VERSION, gtp.getVersion());
        } else if (gtp.getConfigFileName() != null) {
            cv.put(DBContract.GTPackageTable.COL_CONFIG_FILE_NAME, gtp.getConfigFileName());
        } else {
            return;
        }

        String where = String.format("%s = '%s' AND %s = '%s'",
                                    DBContract.GTPackageTable.COL_CODE, gtp.getCode(),
                                    DBContract.GTPackageTable.COL_LANGUAGE, gtp.getLanguage());

        db.update(DBContract.GTPackageTable.TABLE_NAME, cv, where, null);
    }

    public void updateGTLanguage(GTLanguage gtl){
        ContentValues cv = new ContentValues();
        cv.put(DBContract.GTLanguageTable.COL_IS_DOWNLOADED, gtl.isDownloaded());

        String where = String.format("%s = '%s'",
                                    DBContract.GTLanguageTable.COL_CODE, gtl.getLanguageCode());

        db.update(DBContract.GTLanguageTable.TABLE_NAME, cv, where, null);
    }

    private List<GTPackage> queryGTPackage(String selection) {

        String[] projection = {DBContract.GTPackageTable._ID,
                DBContract.GTPackageTable.COL_CODE,
                DBContract.GTPackageTable.COL_NAME,
                DBContract.GTPackageTable.COL_LANGUAGE,
                DBContract.GTPackageTable.COL_VERSION,
                DBContract.GTPackageTable.COL_CONFIG_FILE_NAME,
                DBContract.GTPackageTable.COL_STATUS
        };

        Cursor cursor = db.query(DBContract.GTPackageTable.TABLE_NAME, projection, selection, null, null, null, null);

        List<GTPackage> listGTPackages = new ArrayList<GTPackage>();

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(DBContract.GTPackageTable._ID));
            String code = cursor.getString(cursor.getColumnIndex(DBContract.GTPackageTable.COL_CODE));
            String name = cursor.getString(cursor.getColumnIndex(DBContract.GTPackageTable.COL_NAME));
            String language = cursor.getString(cursor.getColumnIndex(DBContract.GTPackageTable.COL_LANGUAGE));
            double version = cursor.getDouble(cursor.getColumnIndex(DBContract.GTPackageTable.COL_VERSION));
            String configFileName = cursor.getString(cursor.getColumnIndex(DBContract.GTPackageTable.COL_CONFIG_FILE_NAME));
            String status = cursor.getString(cursor.getColumnIndex(DBContract.GTPackageTable.COL_STATUS));

            GTPackage gtPackage = new GTPackage();
            gtPackage.setId(id);
            gtPackage.setCode(code);
            gtPackage.setName(name);
            gtPackage.setLanguage(language);
            gtPackage.setVersion(version);
            gtPackage.setConfigFileName(configFileName);
            gtPackage.setStatus(status);

            listGTPackages.add(gtPackage);
        }

        return listGTPackages;
    }

    private List<GTLanguage> queryGTLanguage(String selection) {
        String[] projection = {DBContract.GTLanguageTable._ID,
                DBContract.GTLanguageTable.COL_CODE,
                DBContract.GTLanguageTable.COL_IS_DOWNLOADED
        };

        Cursor cursor = db.query(DBContract.GTLanguageTable.TABLE_NAME, projection, selection, null, null, null, null);

        List<GTLanguage> listGTLanguages = new ArrayList<GTLanguage>();

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(DBContract.GTLanguageTable._ID));
            String code = cursor.getString(cursor.getColumnIndex(DBContract.GTLanguageTable.COL_CODE));
            boolean isDownloaded = cursor.getInt(cursor.getColumnIndex(DBContract.GTLanguageTable.COL_IS_DOWNLOADED)) > 0;

            GTLanguage gtl = new GTLanguage(code);
            gtl.setId(id);
            gtl.setDownloaded(isDownloaded);

            listGTLanguages.add(gtl);
        }

        return listGTLanguages;
    }

}
