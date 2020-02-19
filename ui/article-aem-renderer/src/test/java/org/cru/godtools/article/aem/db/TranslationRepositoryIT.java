package org.cru.godtools.article.aem.db;

import android.net.Uri;

import org.cru.godtools.article.aem.model.AemImport;
import org.cru.godtools.article.aem.model.TranslationRef;
import org.cru.godtools.article.aem.model.TranslationRefKt;
import org.cru.godtools.model.Translation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Locale;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.annotation.Config.ALL_SDKS;

@RunWith(AndroidJUnit4.class)
@Config(sdk = ALL_SDKS)
public class TranslationRepositoryIT extends BaseArticleRoomDatabaseIT {
    private static final String TOOL = "kgp";
    private static final Locale LANG = Locale.ENGLISH;
    private static final Uri URI1 = Uri.parse("https://example.com/content/experience-fragments/questions_about_god");
    private static final Uri URI2 = Uri.parse("https://example.com/content/experience-fragments/other");

    private Translation mTranslation;
    private TranslationRef.Key mTranslationKey;

    private TranslationRepository mRepository;

    @Before
    public final void setup() {
        mRepository = mDb.translationRepository();

        mTranslation = new Translation();
        mTranslation.setToolCode(TOOL);
        mTranslation.setLanguageCode(LANG);
        mTranslation.setVersion(1);
        mTranslationKey = TranslationRefKt.toTranslationRefKey(mTranslation);
    }

    @Test
    public void verifyAddAemImportsTranslationAlreadyPresent() {
        // setup test
        mDb.translationDao().insertOrIgnore(new TranslationRef(mTranslationKey));

        // perform test
        assertFalse(mRepository.isProcessed(mTranslation));
        mRepository.addAemImports(mTranslation, Arrays.asList(URI1, URI2));
        assertTrue(mRepository.isProcessed(mTranslation));

        // TODO: test AemImports once we define dao methods for reading AemImports

        AemImport aemImport = mDb.aemImportDao().find(URI1);
        assertEquals(URI1, aemImport.getUri());
    }
}
