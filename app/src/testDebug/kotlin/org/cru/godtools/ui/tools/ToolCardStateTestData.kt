package org.cru.godtools.ui.tools

import java.io.File
import java.util.Locale
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation

object ToolCardStateTestData {
    val banner: File = File.createTempFile("tool", "png")

    val tool = ToolCard.State(
        tool = randomTool(
            code = "tool",
            name = "Tool Title",
            category = Tool.CATEGORY_GOSPEL,
            isFavorite = false,
        ),
        banner = banner,
        translation = randomTranslation(
            "tool",
            name = "Translated Tool Title",
            tagline = "Translated Tool Tagline",
        ),
        availableLanguages = 1234,
        appLanguage = Language(Locale.ENGLISH),
        appTranslation = randomTranslation(),
        secondLanguage = Language(Locale.FRENCH),
        secondLanguageAvailable = true
    )

    val toolFavorite = tool.copy(
        tool = randomTool(
            code = "tool",
            name = "Tool Title",
            category = Tool.CATEGORY_GOSPEL,
            isFavorite = true,
        ),
    )
}
