package org.cru.godtools.ui.dashboard.optinnotification

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slack.circuit.overlay.AnimatedOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import kotlinx.coroutines.launch
import org.cru.godtools.R
import org.cru.godtools.util.isTablet

enum class PermissionStatus {
    APPROVED, // Approved
    SOFT_DENIED, // Denied but requestable
    HARD_DENIED, // Denied and no longer requestable
    UNDETERMINED // First time request
}

class OptInNotificationModalOverlay(val requestPermission: suspend () -> Unit, val isHardDenied: Boolean) :
    AnimatedOverlay<Unit>(enterTransition = EnterTransition.None, exitTransition = ExitTransition.None) {

    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<Unit>,
        transitionController: OverlayTransitionController,
    ) {
        BackHandler { navigator.finish(Unit) }

        val isTablet = isTablet()

        val coroutineScope = rememberCoroutineScope()

        val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.2f))
                .navigationBarsPadding()
                .animateEnterExit(enter = EnterTransition.None, exit = ExitTransition.None)
        ) {
            BoxWithConstraints(
                modifier = Modifier.animateEnterExit(
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(
                            durationMillis = 350,
                        ),
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(
                            durationMillis = 350,
                        )
                    )
                )
            ) {
                val screenWidthDp = with(LocalDensity.current) { constraints.maxWidth.toDp() }
                val isLargeTablet = if (isLandscape) screenWidthDp >= 1200.dp else screenWidthDp >= 800.dp
                val paddingFraction = when {
                    isTablet -> when {
                        isLandscape -> if (isLargeTablet) 0.3f else 0.275f
                        else -> if (isLargeTablet) 0.2f else 0.15f
                    }

                    else -> 0.05f
                }

                val cardPadding = screenWidthDp * paddingFraction

                Card(
                    modifier = Modifier
                        .padding(
                            horizontal = cardPadding
                        )
                        .align(Alignment.BottomCenter)
                        .offset(y = 4.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(4.5.dp, MaterialTheme.colorScheme.primary),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 28.dp,
                                end = 28.dp,
                                top = 10.dp,
                                bottom = if (isTablet) 32.dp else 28.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(fraction = 0.95f)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.notification_graphic),
                                contentDescription = null,
                                modifier = if (isTablet) {
                                    Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                } else {
                                    Modifier.fillMaxWidth()
                                },
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.primary,
                                thickness = if (isTablet) 3.2.dp else 2.4.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                            )
                        }
                        BoxWithConstraints {
                            val availableWidth = constraints.maxWidth.dp

                            val titleFontSize = when {
                                availableWidth > 1000.dp -> if (isTablet) 25.sp else 23.sp
                                availableWidth > 800.dp -> if (isTablet) 28.sp else 21.sp
                                availableWidth < 400.dp -> 16.sp
                                else -> if (isTablet) 24.sp else 19.sp
                            }

                            val bodyFontSize = when {
                                availableWidth > 1000.dp -> if (isTablet) 22.sp else 20.sp
                                availableWidth > 800.dp -> if (isTablet) 22.sp else 17.sp
                                availableWidth < 400.dp -> 13.sp
                                else -> if (isTablet) 20.sp else 16.sp
                            }

                            Column {
                                Text(
                                    stringResource(R.string.opt_in_notification_title),
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = titleFontSize,
                                        fontWeight = FontWeight(750),
                                        letterSpacing = 0.9.sp,
                                    ),
                                    modifier = Modifier
                                        .padding(vertical = if (isTablet) 24.dp else 20.dp)
                                        .fillMaxWidth()
                                )
                                Text(
                                    stringResource(R.string.opt_in_notification_body),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontSize = bodyFontSize,
                                        fontWeight = FontWeight(weight = 420),
                                        letterSpacing = 0.9.sp,
                                        lineHeight = if (isTablet) 28.sp else 24.sp,
                                    ),
                                    modifier = Modifier
                                        .padding(bottom = if (isTablet) 26.dp else 22.dp)
                                        .fillMaxWidth()
                                )
                            }
                        }
                        Button(
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(5.dp),
                            onClick = {
                                coroutineScope.launch {
                                    requestPermission()
                                    navigator.finish(Unit)
                                }
                            }
                        ) {
                            Text(
                                if (isHardDenied) {
                                    stringResource(R.string.opt_in_notification_notification_settings)
                                } else {
                                    stringResource(
                                        R.string.opt_in_notification_allow_notifications
                                    )
                                },
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = if (isTablet) 22.sp else 17.sp
                                )
                            )
                        }
                        TextButton(
                            modifier = Modifier
                                .padding(bottom = 40.dp, top = 6.dp)
                                .fillMaxWidth(),
                            onClick = {
                                navigator.finish(Unit)
                            }
                        ) {
                            Text(
                                stringResource(R.string.opt_in_notification_maybe_later),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = if (isTablet) 22.sp else 17.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
