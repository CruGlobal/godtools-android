package org.cru.godtools.article.aem.db;

import org.junit.After;
import org.junit.Before;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

public abstract class BaseArticleRoomDatabaseIT {
    ArticleRoomDatabase mDb;

    @Before
    public final void initDb() {
        mDb = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), ArticleRoomDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    @After
    public final void cleanupDb() {
        mDb.close();
    }
}
