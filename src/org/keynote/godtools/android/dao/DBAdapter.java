package org.keynote.godtools.android.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

    public List<GTPackage> getGTPackageByLanguage(String language){
        String selection = String.format("%s = %s", DBContract.GTPackageTable.COL_LANGUAGE, language);
        return queryGTPackage(selection);
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
            String code = cursor.getString(cursor.getColumnIndex(DBContract.GTPackageTable.COL_CODE));
            String name = cursor.getString(cursor.getColumnIndex(DBContract.GTPackageTable.COL_NAME));
            String language = cursor.getString(cursor.getColumnIndex(DBContract.GTPackageTable.COL_LANGUAGE));
            int version = cursor.getInt(cursor.getColumnIndex(DBContract.GTPackageTable.COL_VERSION));
            String configFileName = cursor.getString(cursor.getColumnIndex(DBContract.GTPackageTable.COL_CONFIG_FILE_NAME));
            String status = cursor.getString(cursor.getColumnIndex(DBContract.GTPackageTable.COL_STATUS));

            GTPackage gtPackage = new GTPackage();
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

}
