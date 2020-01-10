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
     * v4.0.2
     * 2: 2015-05-26
     * v4.0.3 - v4.1.6
     * 3: 2016-02-09
     * v4.1.7
     * 4: 2016-04-01
     * 5: 2016-04-04
     * 6: 2016-04-05
     * v4.2.0 - v4.3.3
     * 7: 2017-04-19
     * 8: 2017-04-19
     * 9: 2017-04-24
     * 10: 2017-04-26
     * 11: 2017-04-27
     * 12: 2017-04-28
     * 13: 2017-04-28
     * 14: 2017-05-03
     * 15: 2017-05-04
     * 16: 2017-05-04
     * 17: 2017-05-05
     * 18: 2017-05-08
     * 19: 2017-05-10
     * 20: 2017-05-11
     * 21: 2017-05-11
     * 22: 2017-05-15
     * 23: 2017-05-15
     * 24: 2017-05-15
     * 25: 2017-06-19
     * 26: 2017-06-26
     * 27: 2017-06-27
     * 28: 2017-06-27
     * 29: 2017-06-27
     * 30: 2017-06-29
     * 31: 2017-06-30
     * 32: 2017-07-06
     * 33: 2017-07-06
     * 34: 2017-07-07
     * 35: 2017-07-07
     * v5.0.0-beta1
     * 36: 2017-07-12
     * v5.0.0 - v5.0.10
     * 37: 2018-04-23
     * v5.0.11 - v5.0.12
     * 38: 2018-06-15
     * v5.0.13 - v5.0.18
     * 39: 2018-10-24
     * 41: 2020-1-10
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
            db.execSQL(Contract.GlobalAnalyticsTable.SQL_CREATE_TABLE);

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
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                        break;
                    case 12:
                        db.execSQL(LanguageTable.SQL_DELETE_TABLE);
                        db.execSQL(LanguageTable.SQL_CREATE_TABLE);
                        break;
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                        break;
                    case 19:
                        db.execSQL(ToolTable.SQL_V19_DROP_LEGACY);
                        db.execSQL(ToolTable.SQL_V19_CREATE_TABLE);
                        break;
                    case 20:
                        db.execSQL(LocalFileTable.SQL_CREATE_TABLE);
                        break;
                    case 21:
                        db.execSQL(TranslationFileTable.SQL_CREATE_TABLE);
                        break;
                    case 22:
                        break;
                    case 23:
                        db.execSQL(AttachmentTable.SQL_CREATE_TABLE);
                        break;
                    case 24:
                        db.execSQL(ToolTable.SQL_V24_ALTER_BANNER);
                        break;
                    case 25:
                        db.execSQL(ToolTable.SQL_V25_ALTER_DETAILS_BANNER);
                        break;
                    case 26:
                        break;
                    case 27:
                        db.execSQL(FollowupTable.SQL_DELETE_TABLE);
                        db.execSQL(FollowupTable.SQL_CREATE_TABLE);
                        break;
                    case 28:
                        db.execSQL(FollowupTable.SQL_V28_MIGRATE_SUBSCRIBERS);
                        break;
                    case 29:
                        db.execSQL(LegacyTables.SQL_DELETE_GSSUBSCRIBERS);
                        break;
                    case 30:
                        db.execSQL(ToolTable.SQL_V30_ALTER_PENDING_SHARES);
                        break;
                    case 31:
                        db.execSQL(ToolTable.SQL_V31_ALTER_CODE);
                        db.execSQL(ToolTable.SQL_V31_ALTER_TYPE);
                        break;
                    case 32:
                        db.execSQL(LegacyTables.SQL_DELETE_GTLANGUAGES);
                        db.execSQL(LegacyTables.SQL_DELETE_GTLANGUAGES_OLD);
                        break;
                    case 33:
                        db.execSQL(LegacyTables.SQL_DELETE_GTPACKAGES);
                        db.execSQL(LegacyTables.SQL_DELETE_GTPACKAGES_OLD);
                        break;
                    case 34:
                        db.execSQL(TranslationTable.SQL_DELETE_TABLE);
                        db.execSQL(TranslationTable.SQL_V34_CREATE_TABLE);

                        db.execSQL(LastSyncTable.SQL_DELETE_TABLE);
                        db.execSQL(LastSyncTable.SQL_CREATE_TABLE);
                        break;
                    case 35:
                        db.execSQL(ToolTable.SQL_V35_UNIQUE_CODE);
                        break;
                    case 36:
                        db.execSQL(TranslationTable.SQL_V36_ALTER_LAST_ACCESSED);
                        db.execSQL(TranslationTable.SQL_V36_POPULATE_LAST_ACCESSED);
                        break;
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
                        db.execSQL(Contract.GlobalAnalyticsTable.SQL_CREATE_TABLE);
                        break;
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
            db.execSQL(Contract.GlobalAnalyticsTable.SQL_DELETE_TABLE);

            // legacy tables
            db.execSQL(LegacyTables.SQL_DELETE_GSSUBSCRIBERS);
            db.execSQL(LegacyTables.SQL_DELETE_GTLANGUAGES);
            db.execSQL(LegacyTables.SQL_DELETE_GTLANGUAGES_OLD);
            db.execSQL(LegacyTables.SQL_DELETE_GTPACKAGES);
            db.execSQL(LegacyTables.SQL_DELETE_GTPACKAGES_OLD);

            onCreate(db);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
