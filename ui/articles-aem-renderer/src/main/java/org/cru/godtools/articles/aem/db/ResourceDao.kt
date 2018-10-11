package org.cru.godtools.articles.aem.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.net.Uri
import android.support.annotation.WorkerThread
import org.cru.godtools.articles.aem.model.Resource

@Dao
interface ResourceDao {
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(resource: Resource)

    @WorkerThread
    @Query("SELECT * FROM resources WHERE uri = :uri")
    fun find(uri: Uri): Resource?
}
