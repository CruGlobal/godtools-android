package org.cru.godtools.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.cru.godtools.R
import org.cru.godtools.model.Language
import org.cru.godtools.model.Translation

@Composable
internal inline fun AvailableInLanguage(
    language: Language?,
    crossinline translation: @DisallowComposableCalls () -> Translation?,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
) {
    val available by remember { derivedStateOf { translation() != null } }
    AvailableInLanguage(
        language,
        available = available,
        horizontalArrangement = horizontalArrangement,
        modifier = modifier
    )
}

@Composable
internal fun AvailableInLanguage(
    language: Language?,
    modifier: Modifier = Modifier,
    available: Boolean = true,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
) = Row(
    horizontalArrangement = horizontalArrangement,
    modifier = modifier.widthIn(min = 50.dp)
) {
    val context = LocalContext.current
    val languageName = remember(language, context) { language?.getDisplayName(context).orEmpty() }

    Text(
        if (available) languageName else stringResource(R.string.tool_card_label_language_unavailable, languageName),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .alignByBaseline()
            .weight(1f, false)
    )
    Icon(
        painterResource(if (available) R.drawable.ic_language_available else R.drawable.ic_language_unavailable),
        contentDescription = null,
        modifier = Modifier
            .padding(start = 4.dp)
            .size(with(LocalDensity.current) { (LocalTextStyle.current.fontSize * 0.65).toDp() })
            .alignBy { it.measuredHeight }
    )
}
