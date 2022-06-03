package org.cru.godtools.ui.about

import android.text.SpannableString
import android.text.style.URLSpan
import android.text.util.Linkify
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme

@Preview(showBackground = true)
@Composable
internal fun AboutLayout() = GodToolsTheme {
    LazyColumn(
        contentPadding = PaddingValues(32.dp, 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val content = listOf(
            R.string.general_about_1,
            R.string.general_about_2,
            R.string.general_about_3,
            R.string.general_about_4,
            R.string.general_about_5,
            R.string.general_about_6,
            R.string.general_about_7,
            R.string.general_about_8,
            R.string.general_about_9,
            R.string.general_about_10
        )

        items(content) {
            val uriHandler = LocalUriHandler.current

            val text = buildAnnotatedString {
                append(stringResource(it))
                linkifyAnnotatedString(Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS)
            }

            ClickableText(
                text,
                style = MaterialTheme.typography.bodyMedium,
                onClick = {
                    text.getUrlSpanAnnotations(it, it).firstOrNull()
                        ?.let { uriHandler.openUri(it.item) }
                }
            )
        }
    }
}

// TODO: extract this to gto-support when needing to reuse it for the first time
@Composable
private fun AnnotatedString.Builder.linkifyAnnotatedString(
    mask: Int,
    linkStyle: SpanStyle? = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline
    )
) {
    val spannable = SpannableString(toAnnotatedString().text)
    Linkify.addLinks(spannable, mask)
    spannable.getSpans(0, spannable.length, URLSpan::class.java).forEach {
        val spanStart = spannable.getSpanStart(it)
        val spanEnd = spannable.getSpanEnd(it)
        addStringAnnotation("URLSpan", it.url, spanStart, spanEnd)
        if (linkStyle != null) addStyle(linkStyle, spanStart, spanEnd)
    }
}

private fun AnnotatedString.getUrlSpanAnnotations(start: Int, end: Int) = getStringAnnotations("URLSpan", start, end)
