package org.cru.godtools.tract.ui.tips

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.tips.Tip
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TipBottomSheetDialogFragmentTest {
    @Test
    fun testCreateFragment() {
        assertNull(TipBottomSheetDialogFragment.create(Tip(Manifest(code = "code", locale = null))))
        assertNull(TipBottomSheetDialogFragment.create(Tip(Manifest(code = null, locale = Locale.ENGLISH))))
        assertNotNull(TipBottomSheetDialogFragment.create(Tip(Manifest(code = "code", locale = Locale.ENGLISH))))
    }
}
