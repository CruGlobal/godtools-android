package org.keynote.godtools.android.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper
{

    private static final int DATABASE_VERSION = 1;
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
        sqLiteDatabase.execSQL(DBContract.GTPackageTable.SQL_DELETE_GTPACKAGES);
        sqLiteDatabase.execSQL(
                DBContract.GTLanguageTable.SQL_DELETE_GTLANGUAGES);
        onCreate(sqLiteDatabase);
    }
}
