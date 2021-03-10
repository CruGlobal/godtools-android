package org.cru.godtools.article.aem.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.junit.After
import org.junit.Before

abstract class BaseArticleRoomDatabaseIT {
    protected lateinit var mDb: ArticleRoomDatabase

    @Before
    fun createDb() {
        mDb = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), ArticleRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() = mDb.close()
}
