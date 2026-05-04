package org.keynote.godtools.android.db

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import org.ccci.gto.android.common.db.CommonTables.LastSyncTable
import org.ccci.gto.android.common.db.WalSQLiteOpenHelper
import org.ccci.gto.android.common.util.content.isApplicationDebuggable
import org.ccci.gto.android.common.util.database.forEach
import org.ccci.gto.android.common.util.database.getString
import org.ccci.gto.android.common.util.database.map
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.AttachmentEntity
import org.cru.godtools.db.room.entity.DownloadedFileEntity
import org.cru.godtools.db.room.entity.DownloadedTranslationFileEntity.Companion.toEntity
import org.cru.godtools.db.room.entity.ToolEntity
import org.cru.godtools.db.room.entity.TranslationEntity
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.Contract.DownloadedFileTable
import org.keynote.godtools.android.db.Contract.FollowupTable
import org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.Contract.LegacyTables
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.Contract.TrainingTipTable
import org.keynote.godtools.android.db.Contract.TranslationFileTable
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.Contract.UserCounterTable
import timber.log.Timber

private const val DATABASE_NAME = "resource.db"
private const val DATABASE_VERSION = 63

/*
 * Version history
 *
 * v6.1.0 - v6.2.0
 * 58: 2023-01-25
 * 59: 2023-05-15
 * 60: 2023-05-09
 * 61: 2023-06-07
 * 62: 2024-01-17
 * 63: 2024-01-17
 */

internal class GodToolsDatabase(private val context: Context, private val roomDb: GodToolsRoomDatabase) :
    WalSQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) = Unit

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            // perform upgrade in increments
            var upgradeTo = oldVersion + 1
            while (upgradeTo <= newVersion) {
                when (upgradeTo) {
                    58 -> {
                        db.query(
                            DownloadedFileTable.TABLE_NAME,
                            arrayOf(DownloadedFileTable.COLUMN_NAME),
                            null,
                            emptyArray(),
                            null,
                            null,
                            null
                        ).use {
                            while (it.moveToNext()) {
                                roomDb.downloadedFilesDao.insertOrIgnoreBlocking(
                                    DownloadedFileEntity(
                                        filename = it.getString(DownloadedFileTable.COLUMN_NAME) ?: continue
                                    )
                                )
                            }
                        }

                        db.execSQL(DownloadedFileTable.SQL_DELETE_TABLE)
                    }

                    59 -> {
                        db.query(
                            ToolTable.TABLE_NAME,
                            ToolTable.PROJECTION_ALL,
                            null,
                            emptyArray(),
                            null,
                            null,
                            null
                        ).use {
                            it.map { ToolMapper.toObject(it) }
                                .filter { it.isValid }
                                .map { ToolEntity(it) }
                                .let { roomDb.toolsDao.insertOrIgnoreTools(it) }
                        }

                        db.execSQL(ToolTable.SQL_DELETE_TABLE)
                    }

                    60 -> {
                        db.query(
                            AttachmentTable.TABLE_NAME,
                            AttachmentTable.PROJECTION_ALL,
                            null,
                            emptyArray(),
                            null,
                            null,
                            null
                        ).use {
                            while (it.moveToNext()) {
                                val attachment = AttachmentMapper.toObject(it)
                                attachment.toolId
                                    ?.let { roomDb.toolsDao.findToolByApiIdBlocking(it) }?.code
                                    ?.let { attachment.toolCode = it }

                                roomDb.attachmentsDao.insertOrIgnore(AttachmentEntity(attachment))
                            }
                        }

                        db.execSQL(AttachmentTable.SQL_DELETE_TABLE)
                    }

                    61 -> {
                        db.query(
                            TranslationTable.TABLE_NAME,
                            TranslationTable.PROJECTION_ALL,
                            null,
                            emptyArray(),
                            null,
                            null,
                            null
                        ).use {
                            it.forEach {
                                val trans = TranslationMapper.toObject(it).takeIf { it.isValid } ?: return@forEach
                                try {
                                    roomDb.translationsDao.insertOrIgnoreTranslationBlocking(TranslationEntity(trans))
                                } catch (_: SQLiteConstraintException) {
                                    // ignore translations that fail FK constraints
                                }
                            }
                        }

                        db.execSQL(TranslationTable.SQL_DELETE_TABLE)
                    }

                    62 -> {
                        db.query(
                            TranslationFileTable.TABLE_NAME,
                            TranslationFileTable.PROJECTION_ALL,
                            null,
                            emptyArray(),
                            null,
                            null,
                            null
                        ).use {
                            it.forEach {
                                val file = TranslationFileMapper.toObject(it)
                                try {
                                    roomDb.downloadedFilesDao.insertOrIgnoreBlocking(file.toEntity())
                                } catch (_: SQLiteConstraintException) {
                                    // ignore files that fail FK constraints
                                }
                            }
                        }

                        db.execSQL(TranslationFileTable.SQL_DELETE_TABLE)
                    }

                    63 -> db.execSQL(LastSyncTable.SQL_DELETE_TABLE)

                    else -> throw SQLiteException("Unrecognized db version:$upgradeTo old:$oldVersion new:$newVersion")
                }

                // perform next upgrade increment
                upgradeTo++
            }
        } catch (e: SQLException) {
            // rethrow exception on debug builds
            if (context.isApplicationDebuggable) throw e

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
            db.execSQL(DownloadedFileTable.SQL_DELETE_TABLE)
            db.execSQL(TranslationFileTable.SQL_DELETE_TABLE)
            db.execSQL(AttachmentTable.SQL_DELETE_TABLE)
            db.execSQL(GlobalActivityAnalyticsTable.SQL_DELETE_TABLE)
            db.execSQL(TrainingTipTable.SQL_DELETE_TABLE)
            db.execSQL(UserCounterTable.SQL_DELETE_TABLE)

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
