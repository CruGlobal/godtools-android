package org.keynote.godtools.android.db

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import org.ccci.gto.android.common.app.ApplicationUtils
import org.ccci.gto.android.common.db.CommonTables.LastSyncTable
import org.ccci.gto.android.common.db.WalSQLiteOpenHelper
import org.cru.godtools.base.util.SingletonHolder
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.Contract.FollowupTable
import org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.Contract.LegacyTables
import org.keynote.godtools.android.db.Contract.LocalFileTable
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.Contract.TranslationFileTable
import org.keynote.godtools.android.db.Contract.TranslationTable
import timber.log.Timber

private const val DATABASE_NAME = "resource.db"
private const val DATABASE_VERSION = 42

/*
 * Version history
 *
 * v5.0.0 - v5.0.10
 * 37: 2018-04-23
 * v5.0.11 - v5.0.12
 * 38: 2018-06-15
 * v5.0.13 - v5.0.18
 * 39: 2018-10-24
 * v5.0.19 - v5.1.4
 * 40: 2019-11-12
 * 41: 2020-01-23
 * v5.1.5 - v5.2.1
 * 42: 2020-05-04
 */

class GodToolsDatabase private constructor(private val context: Context) :
    WalSQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object : SingletonHolder<GodToolsDatabase, Context>({ GodToolsDatabase(it.applicationContext) })

    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.beginTransaction()
            db.execSQL(LastSyncTable.SQL_CREATE_TABLE)
            db.execSQL(FollowupTable.SQL_CREATE_TABLE)
            db.execSQL(LanguageTable.SQL_CREATE_TABLE)
            db.execSQL(ToolTable.SQL_CREATE_TABLE)
            db.execSQL(TranslationTable.SQL_CREATE_TABLE)
            db.execSQL(LocalFileTable.SQL_CREATE_TABLE)
            db.execSQL(TranslationFileTable.SQL_CREATE_TABLE)
            db.execSQL(AttachmentTable.SQL_CREATE_TABLE)
            db.execSQL(GlobalActivityAnalyticsTable.SQL_CREATE_TABLE)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            // perform upgrade in increments
            var upgradeTo = oldVersion + 1
            while (upgradeTo <= newVersion) {
                when (upgradeTo) {
                    37 -> db.execSQL(TranslationTable.SQL_V37_ALTER_TAGLINE)
                    38 -> {
                        db.execSQL(ToolTable.SQL_V38_ALTER_ORDER)
                        db.execSQL(ToolTable.SQL_V38_POPULATE_ORDER)
                    }
                    39 -> db.execSQL(LanguageTable.SQL_V39_ALTER_NAME)
                    40 -> db.execSQL(ToolTable.SQL_V40_ALTER_OVERVIEW_VIDEO)
                    41 -> db.execSQL(GlobalActivityAnalyticsTable.SQL_V41_CREATE_GLOBAL_ANALYTICS)
                    42 -> {
                        db.execSQL(ToolTable.SQL_V42_ALTER_DEFAULT_ORDER)
                        db.execSQL(ToolTable.SQL_V42_POPULATE_DEFAULT_ORDER)
                    }
                    else -> throw SQLiteException("Unrecognized database version")
                }

                // perform next upgrade increment
                upgradeTo++
            }
        } catch (e: SQLException) {
            // rethrow exception on debug builds
            if (ApplicationUtils.isDebuggable(context)) throw e

            // let's try resetting the database instead
            Timber.tag("GodToolsDatabase").e(e, "Error migrating the database")
            resetDatabase(db)
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = resetDatabase(db)

    private fun resetDatabase(db: SQLiteDatabase) {
        try {
            db.beginTransaction()

            // delete any existing tables
            db.execSQL(FollowupTable.SQL_DELETE_TABLE)
            db.execSQL(TranslationTable.SQL_DELETE_TABLE)
            db.execSQL(ToolTable.SQL_DELETE_TABLE)
            db.execSQL(LanguageTable.SQL_DELETE_TABLE)
            db.execSQL(LastSyncTable.SQL_DELETE_TABLE)
            db.execSQL(LocalFileTable.SQL_DELETE_TABLE)
            db.execSQL(TranslationFileTable.SQL_DELETE_TABLE)
            db.execSQL(AttachmentTable.SQL_DELETE_TABLE)
            db.execSQL(GlobalActivityAnalyticsTable.SQL_DELETE_TABLE)

            // legacy tables
            db.execSQL(LegacyTables.SQL_DELETE_GSSUBSCRIBERS)
            db.execSQL(LegacyTables.SQL_DELETE_GTLANGUAGES)
            db.execSQL(LegacyTables.SQL_DELETE_GTLANGUAGES_OLD)
            db.execSQL(LegacyTables.SQL_DELETE_GTPACKAGES)
            db.execSQL(LegacyTables.SQL_DELETE_GTPACKAGES_OLD)
            db.execSQL(LegacyTables.SQL_DELETE_RESOURCES)
            onCreate(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
