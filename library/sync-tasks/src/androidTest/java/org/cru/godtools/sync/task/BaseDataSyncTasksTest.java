package org.cru.godtools.sync.task;

import android.content.Context;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.cru.godtools.api.GodToolsApi;
import org.cru.godtools.model.Language;
import org.junit.Before;
import org.junit.Test;
import org.keynote.godtools.android.db.GodToolsDao;

import java.util.Locale;

import androidx.collection.SimpleArrayMap;
import androidx.test.InstrumentationRegistry;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class BaseDataSyncTasksTest {
    private GodToolsDao mDao;
    private BaseDataSyncTasks mTasks;
    private SimpleArrayMap<Class<?>, Object> mEvents = new SimpleArrayMap<>();

    @Before
    public void setup() {
        final Context context = InstrumentationRegistry.getContext();
        GodToolsApi.configure("https://mobile-content-api-stage.cru.org/");
        mDao = GodToolsDao.Companion.getInstance(context);
        mTasks = new BaseDataSyncTasks(context) {};
    }

    @Test
    public void verifyStoreLanguageChangingCode() {
        // reset database before running test
        mDao.delete(Language.class, null);

        // setup test
        final Locale originalLocale = LocaleCompat.forLanguageTag("lt-LT");
        final Locale newLocale = new Locale("lt");
        mDao.insert(language(1L, originalLocale, true));

        // run test
        mTasks.storeLanguage(mEvents, language(1L, newLocale, false));
        assertThat(mDao.find(Language.class, originalLocale), nullValue());
        final Language language = mDao.find(Language.class, newLocale);
        assertNotNull(language);
        assertEquals(newLocale, language.getCode());
        assertTrue(language.isAdded());
    }

    private Language language(final Long id, final Locale locale, final boolean added) {
        final Language lang = new Language();
        lang.setId(id);
        lang.setCode(locale);
        lang.setAdded(added);
        return lang;
    }
}
