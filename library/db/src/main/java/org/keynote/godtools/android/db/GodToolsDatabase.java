package org.keynote.godtools.android.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.ccci.gto.android.common.app.ApplicationUtils;
import org.ccci.gto.android.common.db.CommonTables.LastSyncTable;
import org.ccci.gto.android.common.db.WalSQLiteOpenHelper;
import org.keynote.godtools.android.db.Contract.AttachmentTable;
import org.keynote.godtools.android.db.Contract.FollowupTable;
import org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable;
import org.keynote.godtools.android.db.Contract.LanguageTable;
import org.keynote.godtools.android.db.Contract.LegacyTables;
import org.keynote.godtools.android.db.Contract.LocalFileTable;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.Contract.TranslationFileTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;

import androidx.annotation.NonNull;
import timber.log.Timber;

public final class GodToolsDatabase extends WalSQLiteOpenHelper {
    private static final String DATABASE_NAME = "resource.db";
    private static final int DATABASE_VERSION = 41;

    /*
     * Version history
     *
     * v5.0.0 - v5.0.10
     * 37: 2018-04-23
     * v5.0.11 - v5.0.12
     * 38: 2018-06-15
     * v5.0.13 - v5.0.18
     * 39: 2018-10-24
     */

    @NonNull
    private final Context mContext;

    private GodToolsDatabase(@NonNull final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @SuppressLint("StaticFieldLeak")
    private static GodToolsDatabase sInstance;

    @NonNull
    public static GodToolsDatabase getInstance(@NonNull final Context context) {
        synchronized (GodToolsDatabase.class) {
            if (sInstance == null) {
                sInstance = new GodToolsDatabase(context.getApplicationContext());
            }
        }

        return sInstance;
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        try {
            db.beginTransaction();

            db.execSQL(LastSyncTable.SQL_CREATE_TABLE);
            db.execSQL(FollowupTable.SQL_CREATE_TABLE);
            db.execSQL(LanguageTable.SQL_CREATE_TABLE);
            db.execSQL(ToolTable.SQL_CREATE_TABLE);
            db.execSQL(TranslationTable.SQL_CREATE_TABLE);
            db.execSQL(LocalFileTable.SQL_CREATE_TABLE);
            db.execSQL(TranslationFileTable.SQL_CREATE_TABLE);
            db.execSQL(AttachmentTable.SQL_CREATE_TABLE);
            db.execSQL(GlobalActivityAnalyticsTable.SQL_CREATE_TABLE);

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
                    case 37:
                        db.execSQL(TranslationTable.SQL_V37_ALTER_TAGLINE);
                        break;
                    case 38:
                        db.execSQL(ToolTable.SQL_V38_ALTER_ORDER);
                        db.execSQL(ToolTable.SQL_V38_POPULATE_ORDER);
                        break;
                    case 39:
                        db.execSQL(LanguageTable.SQL_V39_ALTER_NAME);
                        break;
                    case 40:
                        db.execSQL(ToolTable.SQL_V40_ALTER_OVERVIEW_VIDEO);
                        break;
                    case 41:
                        db.execSQL(GlobalActivityAnalyticsTable.SQL_V41_CREATE_GLOBAL_ANALYTICS);
                    default:
                        // unrecognized version
                        throw new SQLiteException("Unrecognized database version");
                }

                // perform next upgrade increment
                upgradeTo++;
            }
        } catch (final SQLException e) {
            // report (or rethrow) exception
            if (ApplicationUtils.isDebuggable(mContext)) {
                throw e;
            } else {
                Timber.tag("GodToolsDatabase")
                        .e(e, "Error migrating the database");
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
            db.execSQL(FollowupTable.SQL_DELETE_TABLE);
            db.execSQL(TranslationTable.SQL_DELETE_TABLE);
            db.execSQL(ToolTable.SQL_DELETE_TABLE);
            db.execSQL(LanguageTable.SQL_DELETE_TABLE);
            db.execSQL(LastSyncTable.SQL_DELETE_TABLE);
            db.execSQL(LocalFileTable.SQL_DELETE_TABLE);
            db.execSQL(TranslationFileTable.SQL_DELETE_TABLE);
            db.execSQL(AttachmentTable.SQL_DELETE_TABLE);
            db.execSQL(Contract.GlobalActivityAnalyticsTable.SQL_DELETE_TABLE);

            // legacy tables
            db.execSQL(LegacyTables.SQL_DELETE_GSSUBSCRIBERS);
            db.execSQL(LegacyTables.SQL_DELETE_GTLANGUAGES);
            db.execSQL(LegacyTables.SQL_DELETE_GTLANGUAGES_OLD);
            db.execSQL(LegacyTables.SQL_DELETE_GTPACKAGES);
            db.execSQL(LegacyTables.SQL_DELETE_GTPACKAGES_OLD);
            db.execSQL(LegacyTables.SQL_DELETE_RESOURCES);

            onCreate(db);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
