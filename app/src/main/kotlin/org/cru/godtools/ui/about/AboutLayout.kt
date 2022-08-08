package org.cru.godtools.ui.about

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.compose.material3.ui.text.addUriAnnotations
import org.ccci.gto.android.common.androidx.compose.ui.text.getUriAnnotations
import org.cru.godtools.R

@Preview(showBackground = true)
@Composable
internal fun AboutLayout() = LazyColumn(
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

        val text = stringResource(it).addUriAnnotations(Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS)
        ClickableText(
            text,
            style = MaterialTheme.typography.bodyMedium,
            onClick = {
                text.getUriAnnotations(it, it).firstOrNull()
                    ?.let { uriHandler.openUri(it.item) }
            }
        )
    }
}
