package org.cru.godtools.articles.aem.db;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;

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
