package org.cru.godtools.tutorial.layout.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import org.cru.godtools.tutorial.Action
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.layout.TUTORIAL_PAGE_HORIZONTAL_MARGIN

@Composable
@Preview(showBackground = true)
internal fun TutorialOnboardingLinksLayout(
    modifier: Modifier = Modifier,
    onTutorialAction: (Action) -> Unit = {},
) = ConstraintLayout(
    modifier = modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
) {
    val positioning = createTutorialOnboardingPositioning()
    val action = createRef()

    val title = createRef()
    Text(
        text = stringResource(R.string.tutorial_onboarding_links_headline),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .constrainAs(title) { top.linkTo(positioning.title.top) }
            .padding(horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)
            .fillMaxWidth()
    )

    val articles = createRef()
    LinkBox(
        onClick = { onTutorialAction(Action.ONBOARDING_LAUNCH_ARTICLES) },
        description = stringResource(R.string.tutorial_onboarding_links_articles_subhead),
        action = stringResource(R.string.tutorial_onboarding_links_articles_action),
        modifier = Modifier.constrainAs(articles) { top.linkTo(title.bottom, 16.dp) }
    )

    val lessons = createRef()
    LinkBox(
        onClick = { onTutorialAction(Action.ONBOARDING_LAUNCH_LESSONS) },
        description = stringResource(R.string.tutorial_onboarding_links_lessons_subhead),
        action = stringResource(R.string.tutorial_onboarding_links_lessons_action),
        modifier = Modifier.constrainAs(lessons) { top.linkTo(articles.bottom, 16.dp) }
    )

    val tools = createRef()
    LinkBox(
        onClick = { onTutorialAction(Action.ONBOARDING_LAUNCH_TOOLS) },
        description = stringResource(R.string.tutorial_onboarding_links_tools_subhead),
        action = stringResource(R.string.tutorial_onboarding_links_tools_action),
        modifier = Modifier
            .constrainAs(tools) { linkTo(top = lessons.bottom, topMargin = 16.dp, bottom = action.top, bias = 0f) }
    )

    constrain(positioning.chain) { bottom.linkTo(action.top) }
    Button(
        onClick = { onTutorialAction(Action.ONBOARDING_FINISH) },
        modifier = Modifier
            .padding(horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)
            .fillMaxWidth(0.8f)
            .constrainAs(action) {
                centerHorizontallyTo(parent)
                bottom.linkTo(parent.bottom)
            }
    ) { Text(stringResource(R.string.tutorial_onboarding_action_start)) }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LinkBox(
    description: String,
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = Surface(
    onClick = onClick,
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = RectangleShape,
    modifier = modifier
        .padding(horizontal = 24.dp)
        .fillMaxWidth()
) {
    Column(modifier = Modifier.padding(12.dp)) {
        Text(
            description,
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                action,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                painterResource(R.drawable.ic_tutorial_onboarding_link_arrow),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
