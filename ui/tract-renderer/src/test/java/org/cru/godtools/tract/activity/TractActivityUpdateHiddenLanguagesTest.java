package org.cru.godtools.tract.activity;

import org.cru.godtools.model.Translation;
import org.cru.godtools.xml.model.Manifest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;

import java.util.List;
import java.util.Locale;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class TractActivityUpdateHiddenLanguagesTest {
    private TractActivity mActivity;
    @Mock
    private List<Translation> mTranslations;
    @Mock
    private List<Manifest> mManifests;
    private Manifest mManifest;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mActivity = new TractActivity(mTranslations, mManifests);
        mManifest = new Manifest();
    }

    private void setLanguages(final Locale... locales) {
        mActivity.mLanguages = locales;
        mActivity.mHiddenLanguages = new boolean[mActivity.mLanguages.length];
    }

    @Test
    public void verifyPrimaryFirstDownloaded() {
        // setup test
        setLanguages(Locale.FRENCH, Locale.GERMAN, Locale.ITALIAN);
        mActivity.mActiveLanguage = 0;
        mActivity.mPrimaryLanguages = 2;
        whenGetTranslation(0).thenReturn(new Translation());
        whenGetManifest(0).thenReturn(mManifest);

        // run logic and verify results
        mActivity.updateHiddenLanguages();
        assertFalse("first language shouldn't be hidden because it is downloaded", mActivity.mHiddenLanguages[0]);
        assertTrue("second is hidden because first is already downloaded", mActivity.mHiddenLanguages[1]);
        assertFalse(mActivity.mHiddenLanguages[2]);
    }

    @Test
    public void verifyPrimaryFirstLoadingSecondDownloaded() {
        // setup test
        setLanguages(Locale.FRENCH, Locale.GERMAN, Locale.ITALIAN);
        mActivity.mActiveLanguage = 0;
        mActivity.mPrimaryLanguages = 2;
        whenGetTranslation(0).thenReturn(new Translation());
        whenGetTranslation(1).thenReturn(new Translation());
        whenGetManifest(1).thenReturn(mManifest);

        // run logic and verify results
        mActivity.updateHiddenLanguages();
        assertFalse("first language shouldn't be hidden because it is currently active and potentially available",
                    mActivity.mHiddenLanguages[0]);
        assertTrue("second language should be hidden because first language might be available to use as primary",
                   mActivity.mHiddenLanguages[1]);
        assertFalse(mActivity.mHiddenLanguages[2]);
    }

    @Test
    public void verifyPrimaryFirstMissingSecondLoading() {
        // setup test
        setLanguages(Locale.FRENCH, Locale.GERMAN, Locale.ITALIAN);
        mActivity.mActiveLanguage = 2;
        mActivity.mPrimaryLanguages = 2;
        whenGetTranslation(1).thenReturn(new Translation());

        // run logic and verify results
        mActivity.updateHiddenLanguages();
        assertTrue("first language should be hidden because it is not available", mActivity.mHiddenLanguages[0]);
        assertTrue("second language should be hidden because it is still loading", mActivity.mHiddenLanguages[1]);
        assertFalse(mActivity.mHiddenLanguages[2]);
    }

    @Test
    public void verifyPrimaryFirstDownloadedSecondActive() {
        // setup test
        setLanguages(Locale.FRENCH, Locale.GERMAN, Locale.ITALIAN);
        mActivity.mActiveLanguage = 1;
        mActivity.mPrimaryLanguages = 2;
        whenGetTranslation(0).thenReturn(new Translation());
        whenGetTranslation(1).thenReturn(new Translation());
        whenGetManifest(0).thenReturn(mManifest);

        // run logic and verify results
        mActivity.updateHiddenLanguages();
        assertTrue("first language should be hidden because second language is primary and active",
                   mActivity.mHiddenLanguages[0]);
        assertFalse("second language shouldn't be hidden because it is currently active",
                    mActivity.mHiddenLanguages[1]);
        assertFalse(mActivity.mHiddenLanguages[2]);
    }

    private OngoingStubbing<Translation> whenGetTranslation(final int index) {
        return when(mTranslations.get(index));
    }

    private OngoingStubbing<Manifest> whenGetManifest(final int index) {
        return when(mManifests.get(index));
    }
}
