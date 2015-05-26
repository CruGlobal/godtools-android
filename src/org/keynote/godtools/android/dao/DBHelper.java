package org.keynote.godtools.android.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper
{

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "resource.db";


    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        sqLiteDatabase.execSQL(DBContract.GTPackageTable.SQL_CREATE_GTPACKAGES);
        sqLiteDatabase.execSQL(DBContract.GTLanguageTable.SQL_CREATE_GTLANGUAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2)
    {
        // switching to version two adds a name column to the language table
        // the logic below will ensure that no data is lost from the current tables
        if (i2 == 2)
        {
            // rename tables to save data
            sqLiteDatabase.execSQL(DBContract.GTLanguageTable.SQL_RENAME_GTLANGUAGES);
            sqLiteDatabase.execSQL(DBContract.GTPackageTable.SQL_RENAME_GTPACKAGES);

            // create new tables
            onCreate(sqLiteDatabase);

            // copy old data to new tables
            sqLiteDatabase.execSQL(DBContract.GTPackageTable.SQL_COPY_GTPACKAGES);
            sqLiteDatabase.execSQL(DBContract.GTLanguageTable.SQL_COPY_GTLLANGUAGES_V1);

            // delete old tables
            sqLiteDatabase.execSQL(DBContract.GTPackageTable.SQL_DELETE_OLD_GTPACKAGES);
            sqLiteDatabase.execSQL(
                    DBContract.GTLanguageTable.SQL_DELETE_OLD_GTLANGUAGES);
        }
        else
        {
            sqLiteDatabase.execSQL(DBContract.GTPackageTable.SQL_DELETE_GTPACKAGES);
            sqLiteDatabase.execSQL(DBContract.GTLanguageTable.SQL_DELETE_GTLANGUAGES);

            onCreate(sqLiteDatabase);
        }
    }
}
