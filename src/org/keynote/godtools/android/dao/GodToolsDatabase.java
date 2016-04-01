package org.keynote.godtools.android.dao;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Throwables;

import org.ccci.gto.android.common.app.ApplicationUtils;
import org.ccci.gto.android.common.db.WalSQLiteOpenHelper;

import io.fabric.sdk.android.Fabric;

public class GodToolsDatabase extends WalSQLiteOpenHelper {
    private static final String TAG = "GodToolsDatabase";

    /*
     * Version history
     *
     * v4.0.2
     * 2: 2015-05-26
     * v4.0.3 - v4.1.6
     * 3: 2016-02-09
     */

    private static final String DATABASE_NAME = "resource.db";
    private static final int DATABASE_VERSION = 3;

    private static GodToolsDatabase INSTANCE;

    @NonNull
    private final Context mContext;

    private GodToolsDatabase(@NonNull final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @NonNull
    public static GodToolsDatabase getInstance(@NonNull final Context context) {
        synchronized (GodToolsDatabase.class) {
            if (INSTANCE == null) {
                INSTANCE = new GodToolsDatabase(context.getApplicationContext());
            }
        }

        return INSTANCE;
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        try {
            db.beginTransaction();

            db.execSQL(DBContract.GTPackageTable.SQL_CREATE_TABLE);
            db.execSQL(DBContract.GTLanguageTable.SQL_CREATE_GTLANGUAGES);
            db.execSQL(DBContract.GSSubscriberTable.SQL_CREATE_GTSUBSCRIBERS);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            // perform upgrade in increments
            int upgradeTo = oldVersion + 1;
            while (upgradeTo <= newVersion) {
                switch (upgradeTo) {
                    case 2:
                        // rename tables to save data
                        db.execSQL(DBContract.GTLanguageTable.SQL_RENAME_GTLANGUAGES);

                        // create tables
                        db.execSQL(DBContract.GTPackageTable.SQL_V2_CREATE_TABLE);
                        db.execSQL(DBContract.GTLanguageTable.SQL_CREATE_GTLANGUAGES);

                        // copy old data to new table
                        db.execSQL(DBContract.GTLanguageTable.SQL_COPY_GTLLANGUAGES_V1);

                        // delete old table
                        db.execSQL(DBContract.GTLanguageTable.SQL_DELETE_OLD_GTLANGUAGES);
                        break;
                    case 3:
                        // rename old packages table
                        db.execSQL(DBContract.GTPackageTable.SQL_DELETE_OLD_TABLE);
                        db.execSQL(DBContract.GTPackageTable.SQL_RENAME_TABLE);

                        // create new table
                        db.execSQL(DBContract.GTPackageTable.SQL_CREATE_TABLE);

                        // migrate data
                        db.execSQL(DBContract.GTPackageTable.SQL_V3_MIGRATE_DATA);

                        // delete old table
                        db.execSQL(DBContract.GTPackageTable.SQL_DELETE_OLD_TABLE);

                        break;
                    default:
                        // unrecognized version
                        throw new SQLiteException("Unrecognized database version");
                }

                // perform next upgrade increment
                upgradeTo++;
            }
        } catch (final SQLException e) {
            Log.e(TAG, "error upgrading database", e);

            // report (or rethrow) exception
            if (ApplicationUtils.isDebuggable(mContext)) {
                throw Throwables.propagate(e);
            } else if (Fabric.isInitialized() && Crashlytics.getInstance() != null) {
                Crashlytics.logException(e);
            }

            // let's try resetting the database instead
            resetDatabase(db);
        }
    }

    @Override
    public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // reset the database, don't try and downgrade tables
        resetDatabase(db);
    }

    private void resetDatabase(final SQLiteDatabase db) {
        try {
            db.beginTransaction();

            // delete any existing tables
            db.execSQL(DBContract.GTPackageTable.SQL_DELETE_TABLE);
            db.execSQL(DBContract.GTPackageTable.SQL_DELETE_OLD_TABLE);
            db.execSQL(DBContract.GTLanguageTable.SQL_DELETE_GTLANGUAGES);
            db.execSQL(DBContract.GTLanguageTable.SQL_DELETE_OLD_GTLANGUAGES);
            db.execSQL(DBContract.GSSubscriberTable.SQL_DELETE_GTSUBSCRIBERS);

            onCreate(db);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
