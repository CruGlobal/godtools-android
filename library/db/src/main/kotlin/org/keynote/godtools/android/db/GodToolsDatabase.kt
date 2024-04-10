package org.keynote.godtools.android.db

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import java.time.Instant
import org.ccci.gto.android.common.db.CommonTables.LastSyncTable
import org.ccci.gto.android.common.db.WalSQLiteOpenHelper
import org.ccci.gto.android.common.db.util.CursorUtils.getBool
import org.ccci.gto.android.common.util.content.isApplicationDebuggable
import org.ccci.gto.android.common.util.database.forEach
import org.ccci.gto.android.common.util.database.getDouble
import org.ccci.gto.android.common.util.database.getInt
import org.ccci.gto.android.common.util.database.getLocale
import org.ccci.gto.android.common.util.database.getLong
import org.ccci.gto.android.common.util.database.getString
import org.ccci.gto.android.common.util.database.map
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.AttachmentEntity
import org.cru.godtools.db.room.entity.DownloadedFileEntity
import org.cru.godtools.db.room.entity.DownloadedTranslationFileEntity.Companion.toEntity
import org.cru.godtools.db.room.entity.FollowupEntity
import org.cru.godtools.db.room.entity.LanguageEntity
import org.cru.godtools.db.room.entity.ToolEntity
import org.cru.godtools.db.room.entity.TrainingTipEntity
import org.cru.godtools.db.room.entity.TranslationEntity
import org.cru.godtools.db.room.entity.partial.MigrationGlobalActivity
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
 * v5.7.0
 * 48: 2022-02-14
 * 49: 2022-04-08
 * 50: 2022-04-29
 * 51: 2022-05-02
 * v6.0.0 - v6.0.1
 * 52: 2022-09-22
 * 53: 2022-09-23
 * 54: 2022-11-04
 * 55: 2022-11-22
 * 56: 2022-12-06
 * 57: 2022-12-06
 * v6.1.0 - v6.2.0
 * 58: 2023-01-25
 * 59: 2023-05-15
 * 60: 2023-05-09
 * 61: 2023-06-07
 * 62: 2024-01-17
 * 63: 2024-01-17
 */

internal class GodToolsDatabase(
    private val context: Context,
    private val roomDb: GodToolsRoomDatabase,
) : WalSQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) = Unit

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            // perform upgrade in increments
            var upgradeTo = oldVersion + 1
            while (upgradeTo <= newVersion) {
                when (upgradeTo) {
                    48 -> {
                        db.execSQL(ToolTable.SQL_V48_CREATE_SPOTLIGHT)
                        db.execSQL(ToolTable.SQL_V48_POPULATE_SPOTLIGHT)
                    }
                    49 -> db.execSQL(ToolTable.SQL_V49_ALTER_DETAILS_BANNER_ANIMATION)
                    50 -> db.execSQL(ToolTable.SQL_V50_ALTER_META_TOOL)
                    51 -> db.execSQL(ToolTable.SQL_V51_ALTER_DEFAULT_VARIANT)
                    52 -> {
                        db.query(
                            UserCounterTable.TABLE_NAME,
                            arrayOf(
                                UserCounterTable.COLUMN_COUNTER_ID,
                                UserCounterTable.COLUMN_COUNT,
                                UserCounterTable.COLUMN_DECAYED_COUNT,
                                UserCounterTable.COLUMN_DELTA
                            ),
                            null,
                            emptyArray(),
                            null,
                            null,
                            null
                        ).use {
                            while (it.moveToNext()) {
                                roomDb.userCountersRepository.migrateCounter(
                                    it.getString(UserCounterTable.COLUMN_COUNTER_ID) ?: continue,
                                    it.getInt(UserCounterTable.COLUMN_COUNT, 0),
                                    it.getDouble(UserCounterTable.COLUMN_DECAYED_COUNT, 0.0),
                                    it.getInt(UserCounterTable.COLUMN_DELTA, 0),
                                )
                            }
                        }

                        db.execSQL(UserCounterTable.SQL_DELETE_TABLE)
                    }
                    53 -> {
                        db.query(
                            GlobalActivityAnalyticsTable.TABLE_NAME,
                            arrayOf(
                                GlobalActivityAnalyticsTable.COLUMN_USERS,
                                GlobalActivityAnalyticsTable.COLUMN_COUNTRIES,
                                GlobalActivityAnalyticsTable.COLUMN_LAUNCHES,
                                GlobalActivityAnalyticsTable.COLUMN_GOSPEL_PRESENTATIONS
                            ),
                            null,
                            emptyArray(),
                            null,
                            null,
                            null
                        ).use {
                            if (it.moveToFirst()) {
                                roomDb.globalActivityDao.insertOrIgnore(
                                    MigrationGlobalActivity(
                                        it.getInt(GlobalActivityAnalyticsTable.COLUMN_USERS, 0),
                                        it.getInt(GlobalActivityAnalyticsTable.COLUMN_COUNTRIES, 0),
                                        it.getInt(GlobalActivityAnalyticsTable.COLUMN_LAUNCHES, 0),
                                        it.getInt(GlobalActivityAnalyticsTable.COLUMN_GOSPEL_PRESENTATIONS, 0),
                                    )
                                )
                            }
                        }

                        db.execSQL(GlobalActivityAnalyticsTable.SQL_DELETE_TABLE)
                    }
                    54 -> {
                        db.query(
                            TrainingTipTable.TABLE_NAME,
                            arrayOf(
                                TrainingTipTable.COLUMN_TOOL,
                                TrainingTipTable.COLUMN_LANGUAGE,
                                TrainingTipTable.COLUMN_TIP_ID,
                                TrainingTipTable.COLUMN_IS_COMPLETED
                            ),
                            null,
                            emptyArray(),
                            null,
                            null,
                            null
                        ).use {
                            while (it.moveToNext()) {
                                roomDb.trainingTipDao.insertOrIgnoreBlocking(
                                    TrainingTipEntity(
                                        TrainingTipEntity.Key(
                                            tool = it.getString(TrainingTipTable.COLUMN_TOOL) ?: continue,
                                            locale = it.getLocale(TrainingTipTable.COLUMN_LANGUAGE) ?: continue,
                                            tipId = it.getString(TrainingTipTable.COLUMN_TIP_ID) ?: continue,
                                        )
                                    ).apply {
                                        isCompleted = getBool(it, TrainingTipTable.COLUMN_IS_COMPLETED, false)
                                        isNew = true
                                    }
                                )
                            }
                        }

                        db.execSQL(TrainingTipTable.SQL_DELETE_TABLE)
                    }
                    55 -> {
                        db.query(
                            FollowupTable.TABLE_NAME,
                            arrayOf(
                                FollowupTable.COLUMN_NAME,
                                FollowupTable.COLUMN_EMAIL,
                                FollowupTable.COLUMN_LANGUAGE,
                                FollowupTable.COLUMN_DESTINATION,
                                FollowupTable.COLUMN_CREATE_TIME
                            ),
                            null,
                            emptyArray(),
                            null,
                            null,
                            null
                        ).use {
                            while (it.moveToNext()) {
                                roomDb.followupsDao.insertBlocking(
                                    FollowupEntity(
                                        name = it.getString(FollowupTable.COLUMN_NAME),
                                        email = it.getString(FollowupTable.COLUMN_EMAIL) ?: continue,
                                        language = it.getLocale(FollowupTable.COLUMN_LANGUAGE) ?: continue,
                                        destination = it.getLong(FollowupTable.COLUMN_DESTINATION) ?: continue,
                                        createdAt = it.getLong(FollowupTable.COLUMN_CREATE_TIME)
                                            ?.let { Instant.ofEpochMilli(it) } ?: Instant.now()
                                    )
                                )
                            }
                        }

                        db.execSQL(FollowupTable.SQL_DELETE_TABLE)
                    }
                    56 -> {
                        db.query(
                            LanguageTable.TABLE_NAME,
                            arrayOf(
                                LanguageTable.COLUMN_ID,
                                LanguageTable.COLUMN_CODE,
                                LanguageTable.COLUMN_NAME
                            ),
                            null,
                            emptyArray(),
                            null,
                            null,
                            null
                        ).use {
                            roomDb.languagesDao.insertOrIgnoreLanguages(
                                it.map {
                                    LanguageEntity(
                                        code = it.getLocale(LanguageTable.COLUMN_CODE) ?: return@map null,
                                        name = it.getString(LanguageTable.COLUMN_NAME),
                                        apiId = it.getLong(LanguageTable.COLUMN_ID),
                                    )
                                }.filterNotNull()
                            )
                        }

                        db.execSQL(LanguageTable.SQL_DELETE_TABLE)
                    }
                    57 -> {
                        db.execSQL(TranslationTable.SQL_V57_ALTER_DETAILS_OUTLINE)
                        db.execSQL(TranslationTable.SQL_V57_ALTER_DETAILS_BIBLE_REFERENCES)
                        db.execSQL(TranslationTable.SQL_V57_ALTER_DETAILS_CONVERSATION_STARTERS)
                    }
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
