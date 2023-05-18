package org.cru.godtools.db.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.db.room.entity.TranslationEntity
import org.cru.godtools.db.room.entity.partial.SyncTranslation

@Dao
internal interface TranslationsDao {
    @Query("SELECT * FROM translations WHERE id = :id")
    suspend fun findTranslation(id: Long): TranslationEntity?
    @Query("SELECT * FROM translations WHERE id = :id")
    fun findTranslationBlocking(id: Long): TranslationEntity?

    @Query("SELECT * FROM translations")
    suspend fun getTranslations(): List<TranslationEntity>
    @Query("SELECT * FROM translations WHERE locale IN (:languages)")
    suspend fun getTranslationsForLanguages(languages: Collection<Locale>): List<TranslationEntity>
    @Query("SELECT * FROM translations WHERE tool = :tool")
    fun getTranslationsForToolBlocking(tool: String): List<TranslationEntity>
    @Query("SELECT * FROM translations")
    fun getTranslationsFlow(): Flow<List<TranslationEntity>>
    @Query("SELECT * FROM translations WHERE tool IN (:tools)")
    fun getTranslationsForToolsFlow(tools: Collection<String>): Flow<List<TranslationEntity>>
    @Query("SELECT * FROM translations WHERE tool IN (:tools) AND locale IN (:locales)")
    fun getTranslationsForToolsAndLocalesFlow(
        tools: Collection<String>,
        locales: Collection<Locale>,
    ): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translations WHERE tool = :tool AND locale = :language ORDER BY version DESC")
    suspend fun getLatestTranslations(tool: String, language: Locale): List<TranslationEntity>
    @Query("SELECT * FROM translations WHERE tool = :tool AND locale = :language ORDER BY version DESC")
    fun getLatestTranslationsFlow(tool: String, language: Locale): Flow<List<TranslationEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreTranslations(translations: Collection<TranslationEntity>)
    @Upsert(entity = TranslationEntity::class)
    fun upsertBlocking(translation: SyncTranslation)
    @Query("UPDATE translations SET isDownloaded = :isDownloaded WHERE id = :id")
    suspend fun updateTranslationDownloaded(id: Long, isDownloaded: Boolean)

//    @Query("UPDATE translations SET lastAccessed = :accessTime WHERE id = :id")
//    suspend fun updateTranslationLastAccessed(id: Long, accessTime: Instant)
    @Delete
    fun deleteBlocking(translation: TranslationEntity)
}
