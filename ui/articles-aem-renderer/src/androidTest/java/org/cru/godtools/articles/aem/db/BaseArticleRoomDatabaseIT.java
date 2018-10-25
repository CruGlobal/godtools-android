package org.cru.godtools.articles.aem.db;

import android.content.Context;

import org.junit.After;
import org.junit.Before;

import androidx.room.Room;
import androidx.test.InstrumentationRegistry;

public abstract class BaseArticleRoomDatabaseIT {
    Context mContext;
    ArticleRoomDatabase mDb;

    @Before
    public final void initDb() {
        mContext = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, ArticleRoomDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    @After
    public final void cleanupDb() {
        mDb.close();
    }
}
