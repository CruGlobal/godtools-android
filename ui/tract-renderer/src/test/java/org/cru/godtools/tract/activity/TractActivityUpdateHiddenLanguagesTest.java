package org.cru.godtools.tract.activity;

import android.graphics.Color;
import android.util.SparseArray;

import com.google.common.util.concurrent.SettableFuture;

import org.ccci.gto.android.common.testing.CommonMocks;
import org.cru.godtools.model.Translation;
import org.cru.godtools.tract.model.Manifest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import java.util.Locale;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@PrepareForTest({Color.class})
public class TractActivityUpdateHiddenLanguagesTest {
    @Rule
    public final PowerMockRule mPowerMockRule = new PowerMockRule();

    private TractActivity mActivity;
    @Mock
    private SparseArray<Translation> mTranslations;
    @Mock
    private SparseArray<Manifest> mManifests;
    private Manifest mManifest;

    @Before
    public void setup() {
        CommonMocks.mockColor();
        MockitoAnnotations.initMocks(this);

        mActivity = new TractActivity(mTranslations, mManifests);
        mManifest = new Manifest();
    }

    private void setLanguages(final Locale... locales) {
        mActivity.mLanguages = locales;
        mActivity.mDownloadTasks = new SettableFuture[mActivity.mLanguages.length];
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
        translationMissing(0);
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
        when(mTranslations.indexOfKey(index)).thenReturn(0);
        return when(mTranslations.get(index));
    }

    private void translationMissing(final int index) {
        when(mTranslations.indexOfKey(index)).thenReturn(-1);
    }

    private OngoingStubbing<Manifest> whenGetManifest(final int index) {
        return when(mManifests.get(index));
    }
}
