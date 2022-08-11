package org.cru.godtools.tutorial.layout.liveshare

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.ccci.gto.android.common.androidx.compose.ui.text.computeHeightForDefaultText
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.layout.TUTORIAL_PAGE_HORIZONTAL_MARGIN
import org.cru.godtools.tutorial.layout.TutorialMedia

@Composable
internal fun TutorialLiveShareLayout(
    page: Page,
    modifier: Modifier = Modifier,
    nextPage: () -> Unit = {},
    onTutorialAction: (Int) -> Unit = {},
) = Column(
    modifier = modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
) {
    val titleStyle = MaterialTheme.typography.titleLarge
    val contentStyle = MaterialTheme.typography.bodyLarge

    Spacer(modifier = Modifier.weight(1f))

    Column(
        modifier = Modifier
            .padding(horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)
            .heightIn(
                min = computeHeightForDefaultText(titleStyle, 1) + 16.dp + computeHeightForDefaultText(contentStyle, 5)
            )
    ) {
        Text(
            text = page.title?.let { stringResource(it) }.orEmpty(),
            style = titleStyle,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            page.content?.let { stringResource(it) }.orEmpty(),
            style = contentStyle,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        )
    }

    TutorialMedia(
        page,
        modifier = Modifier
            .padding(
                top = 16.dp,
                horizontal = if (page == Page.LIVE_SHARE_DESCRIPTION) TUTORIAL_PAGE_HORIZONTAL_MARGIN else 0.dp
            )
            .fillMaxWidth()
            .height(266.dp)
    )

    Spacer(modifier = Modifier.weight(1f))

    Button(
        onClick = {
            when (page) {
                Page.LIVE_SHARE_START -> onTutorialAction(R.id.action_live_share_finish)
                else -> nextPage()
            }
        },
        modifier = Modifier
            .padding(horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)
            .fillMaxWidth(0.8f)
            .align(Alignment.CenterHorizontally)
    ) { Text(page.action?.let { stringResource(it) }.orEmpty()) }
}
