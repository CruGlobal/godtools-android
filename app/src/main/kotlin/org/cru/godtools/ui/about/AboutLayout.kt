package org.cru.godtools.ui.about

import android.text.util.Linkify
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.compose.material3.ClickableText
import org.ccci.gto.android.common.androidx.compose.material3.ui.text.addUriAnnotations
import org.ccci.gto.android.common.androidx.compose.ui.text.getUriAnnotations
import org.cru.godtools.R

@Preview(showBackground = true)
@Composable
internal fun AboutLayout() = Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 32.dp, vertical = 16.dp)
) {
    val uriHandler = LocalUriHandler.current

    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
        listOf(
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
        ).forEach {
            val text = stringResource(it).addUriAnnotations(Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS)
            ClickableText(
                text,
                onClick = {
                    text.getUriAnnotations(it, it).firstOrNull()
                        ?.let { uriHandler.openUri(it.item) }
                }
            )
        }
    }
}
