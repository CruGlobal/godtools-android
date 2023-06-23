package org.cru.godtools.db.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.db.room.entity.LanguageEntity
import org.cru.godtools.db.room.entity.partial.SyncLanguage

@Dao
internal interface LanguagesDao {
    @Query("SELECT * FROM languages WHERE code = :locale")
    suspend fun findLanguage(locale: Locale): LanguageEntity?
    @Query("SELECT * FROM languages WHERE code = :locale")
    fun findLanguageFlow(locale: Locale): Flow<LanguageEntity?>

    @Query("SELECT * FROM languages")
    suspend fun getLanguages(): List<LanguageEntity>
    @Query("SELECT * FROM languages")
    fun getLanguagesFlow(): Flow<List<LanguageEntity>>
    @Query("SELECT * FROM languages WHERE code IN(:locales)")
    fun getLanguagesFlow(locales: Collection<Locale>): Flow<List<LanguageEntity>>
    @Query("SELECT * FROM languages WHERE isAdded = 1")
    fun getPinnedLanguagesFlow(): Flow<List<LanguageEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnoreLanguages(languages: Collection<LanguageEntity>)
    @Upsert(entity = LanguageEntity::class)
    fun upsertLanguagesBlocking(languages: Collection<SyncLanguage>)

    @Query("UPDATE languages SET isAdded = :isAdded WHERE code = :locale")
    suspend fun markLanguageAdded(locale: Locale, isAdded: Boolean)

    @Delete
    suspend fun deleteLanguages(languages: Collection<LanguageEntity>)
}
