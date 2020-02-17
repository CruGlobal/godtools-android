package org.cru.godtools.article.aem.db;

import android.content.Context;

import org.junit.After;
import org.junit.Before;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

public abstract class BaseArticleRoomDatabaseIT {
    Context mContext;
    ArticleRoomDatabase mDb;

    @Before
    public final void initDb() {
        mContext = ApplicationProvider.getApplicationContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, ArticleRoomDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    @After
    public final void cleanupDb() {
        mDb.close();
    }
}
