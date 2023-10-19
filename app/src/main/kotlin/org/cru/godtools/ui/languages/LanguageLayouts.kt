package org.cru.godtools.ui.languages

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import java.util.Locale
import org.cru.godtools.base.LocalAppLanguage
import org.cru.godtools.model.Language

@Composable
internal fun LanguageName(locale: Locale) =
    LanguageName(locale.getDisplayName(LocalAppLanguage.current), locale.getDisplayName(locale))

@Composable
internal fun LanguageName(language: Language, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    LanguageName(
        language.getDisplayName(context, LocalAppLanguage.current),
        language.getDisplayName(context, language.code),
        modifier
    )
}

private const val LANGUAGE_NAME_GAP = "[gap]"

@Composable
private fun LanguageName(displayName: String, nativeName: String, modifier: Modifier = Modifier) {
    val color = LocalTextStyle.current.color.takeOrElse { LocalContentColor.current }
    val displayNameColor = color.let { it.copy(alpha = it.alpha * 0.60f) }

    Text(
        remember(displayName, nativeName, color, displayNameColor) {
            buildAnnotatedString {
                withStyle(SpanStyle(color = color)) { append(nativeName) }
                appendInlineContent(LANGUAGE_NAME_GAP, " ")
                withStyle(SpanStyle(color = displayNameColor)) { append(displayName) }
            }
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        inlineContent = remember {
            mapOf(
                LANGUAGE_NAME_GAP to InlineTextContent(
                    Placeholder(
                        width = 8.sp,
                        height = 1.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline
                    )
                ) {}
            )
        },
        modifier = modifier
    )
}
