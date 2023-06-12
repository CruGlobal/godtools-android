package org.cru.godtools.tutorial.layout.onboarding

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.tutorial.Action
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.layout.TUTORIAL_PAGE_HORIZONTAL_MARGIN

@Composable
@Preview(showBackground = true, heightDp = 800)
internal fun TutorialOnboardingWelcomeLayout(
    modifier: Modifier = Modifier,
    nextPage: () -> Unit = {},
    onTutorialAction: (Action) -> Unit = {},
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
) {
    // region Welcome animation (Transition model)
    var welcomeState by rememberSaveable { mutableStateOf(WelcomeState.WELCOME) }
    LaunchedEffect(Unit) {
        delay(2000)
        welcomeState = WelcomeState.READY
    }

    val welcomeTransition = updateTransition(targetState = welcomeState, label = "Welcome Transition")
    val welcomeAlpha by welcomeTransition.animateFloat(
        transitionSpec = { tween(1000) },
        label = "Welcome Visibility",
    ) { if (it == WelcomeState.WELCOME) 1f else 0f }
    val readyAlpha by welcomeTransition.animateFloat(
        transitionSpec = { tween(1000, 600) },
        label = "Ready Visibility"
    ) { if (it == WelcomeState.READY) 1f else 0f }
    // endregion Welcome animation (Transition model)

    FilledTonalButton(
        onClick = { onTutorialAction(Action.ONBOARDING_CHANGE_LANGUAGE) },
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        enabled = welcomeState == WelcomeState.READY,
        modifier = Modifier.alpha(readyAlpha)
    ) {
        Icon(Icons.Outlined.Translate, null, tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.tutorial_onboarding_change_language), modifier = Modifier.padding(start = 8.dp))
    }

    Spacer(modifier = Modifier.weight(2f))

    Image(
        painterResource(org.cru.godtools.ui.R.mipmap.ic_launcher_foreground),
        contentDescription = null,
        modifier = Modifier.size(130.dp)
    )
    Image(
        painterResource(R.drawable.ic_logo_title),
        contentDescription = null,
        modifier = Modifier.wrapContentSize()
    )

    Box(modifier = Modifier.padding(top = 24.dp, horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)) {
        Text(
            text = stringResource(R.string.tutorial_onboarding_welcome),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(welcomeAlpha)
        )
        Text(
            text = stringResource(R.string.tutorial_onboarding_welcome_2),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(readyAlpha)
        )
    }

    Spacer(modifier = Modifier.weight(1f))

    TextButton(
        onClick = { onTutorialAction(Action.ONBOARDING_WATCH_VIDEO) },
        enabled = readyAlpha > 0f,
        modifier = Modifier
            .padding(horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)
            .alpha(readyAlpha)
    ) {
        Text(
            text = stringResource(R.string.tutorial_onboarding_welcome_action_video),
            fontWeight = FontWeight.Bold
        )
        Icon(
            painterResource(R.drawable.ic_tutorial_onboarding_watch_video),
            contentDescription = null,
            modifier = Modifier.padding(start = 4.dp)
        )
    }

    Spacer(modifier = Modifier.weight(1f))

    Button(
        onClick = nextPage,
        modifier = Modifier
            .padding(horizontal = TUTORIAL_PAGE_HORIZONTAL_MARGIN)
            .fillMaxWidth(0.8f)
            .align(Alignment.CenterHorizontally)
    ) { Text(stringResource(R.string.tutorial_onboarding_action_begin)) }
}

private enum class WelcomeState { WELCOME, READY }
