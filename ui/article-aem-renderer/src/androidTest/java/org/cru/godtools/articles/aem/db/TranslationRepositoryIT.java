package org.cru.godtools.articles.aem.db;

import android.net.Uri;

import com.google.common.collect.ImmutableList;

import org.cru.godtools.articles.aem.model.AemImport;
import org.cru.godtools.articles.aem.model.TranslationRef;
import org.cru.godtools.model.Translation;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        mTranslationKey = TranslationRef.Key.from(mTranslation);
    }

    @Test
    public void verifyIsProcessedTrue() {
        // setup test
        final TranslationRef translation = new TranslationRef(mTranslationKey);
        translation.processed = true;
        mDb.translationDao().insertOrIgnore(translation);

        // perform test
        assertTrue(mRepository.isProcessed(mTranslation));
    }

    @Test
    public void verifyIsProcessedFalse() {
        // setup test
        final TranslationRef translation = new TranslationRef(mTranslationKey);
        mDb.translationDao().insertOrIgnore(translation);

        // perform test
        assertFalse(mRepository.isProcessed(mTranslation));
    }

    @Test
    public void verifyIsProcessedNotPresent() {
        assertFalse(mRepository.isProcessed(mTranslation));
    }

    @Test
    public void verifyAddAemImportsTranslationAlreadyPresent() {
        // setup test
        mDb.translationDao().insertOrIgnore(new TranslationRef(mTranslationKey));

        // perform test
        assertFalse(mRepository.isProcessed(mTranslation));
        mRepository.addAemImports(mTranslation, ImmutableList.of(URI1, URI2));
        assertTrue(mRepository.isProcessed(mTranslation));

        // TODO: test AemImports once we define dao methods for reading AemImports

        AemImport aemImport = mDb.aemImportDao().find(URI1);
        assertEquals(URI1, aemImport.getUri());
    }
}
