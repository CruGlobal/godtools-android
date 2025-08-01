package org.cru.godtools.tool.tips.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.fluidsonic.locale.toCommon
import java.util.Locale
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.tips.Tip
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TipBottomSheetDialogFragmentTest {
    @Test
    fun testCreateFragment() {
        assertNull(TipBottomSheetDialogFragment.create(Tip(Manifest(code = "code", locale = null))))
        assertNull(TipBottomSheetDialogFragment.create(Tip(Manifest(code = null, locale = Locale.ENGLISH.toCommon()))))
        assertNotNull(
            TipBottomSheetDialogFragment.create(Tip(Manifest(code = "code", locale = Locale.ENGLISH.toCommon())))
        )
    }
}
