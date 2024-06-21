package org.cru.godtools.ui.languages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.util.Locale
import org.cru.godtools.model.Language
import org.cru.godtools.ui.BasePaparazziTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class LanguageNamePaparazziTest(@TestParameter nightMode: NightMode) :
    BasePaparazziTest(nightMode = nightMode, renderingMode = RenderingMode.SHRINK) {
    private val forcedNameLanguage = Language(
        code = Locale.ENGLISH,
        name = "Forced Language Name",
        isForcedName = true,
    )

    @Test
    fun `LanguageNames()`() = centerInSnapshot {
        Column(Modifier.padding(16.dp)) {
            LanguageName(Locale("ar"))
            HorizontalDivider()
            LanguageName(Locale.forLanguageTag("bs-BA"))
            HorizontalDivider()
            LanguageName(Locale.ENGLISH)
            HorizontalDivider()
            LanguageName(language = forcedNameLanguage)
        }
    }
}
