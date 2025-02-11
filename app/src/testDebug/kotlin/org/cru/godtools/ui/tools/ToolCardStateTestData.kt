package org.cru.godtools.ui.tools

import android.graphics.drawable.Drawable
import java.io.File
import java.util.Locale
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation

object ToolCardStateTestData {
    val banner: File = File.createTempFile("tool", "png")
    val bannerDrawable by lazy {
        Drawable.createFromStream(this.javaClass.getResourceAsStream("banner.jpg"), "banner.jpg")!!
    }

    val tool = ToolCard.State(
        tool = randomTool(
            code = "tool",
            name = "Tool Title",
            category = Tool.CATEGORY_GOSPEL,
            isFavorite = false,
        ),
        banner = banner,
        language = Language(Locale.ENGLISH),
        languageAvailable = true,
        translation = randomTranslation(
            "tool",
            name = "Translated Tool Title",
            tagline = "Translated Tool Tagline",
        ),
        availableLanguages = 1234,
        appLanguage = Language(Locale.ENGLISH),
        appLanguageAvailable = true,
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
