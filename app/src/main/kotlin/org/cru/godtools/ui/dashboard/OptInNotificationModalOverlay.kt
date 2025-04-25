package org.cru.godtools.ui.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.util.isTablet

class OptInNotificationModalOverlay(
    val requestPermission: suspend () -> Unit,
    val isHardDenied: Boolean,
) : Overlay<Unit> {


    @Composable
    override fun Content(navigator: OverlayNavigator<Unit>) {
        BackHandler { navigator.finish(Unit) }

        val isTablet = isTablet()

        val coroutineScope = rememberCoroutineScope()
        val transitionState = remember { MutableTransitionState(false) }

        LaunchedEffect(Unit) { transitionState.targetState = true }

        LaunchedEffect(transitionState.currentState, transitionState.targetState) {
            if (!transitionState.currentState && transitionState.isIdle) {
                navigator.finish(Unit)
            }
        }

        Surface(
            color = Color.Transparent, modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
        ) {
            AnimatedVisibility(
                visibleState = transitionState,
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
                    ),
                ),
            ) {
                BoxWithConstraints {
                    val totalWidth = constraints.maxWidth.dp
                    val isLargeTablet = totalWidth > 1700.dp

                    println("totalWidth: $totalWidth")
                    Card(
                        modifier = Modifier
                            .padding(horizontal = if (isTablet) if (isLargeTablet) 200.dp else 150.dp else 16.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = 4.dp)
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(4.5.dp, MaterialTheme.colorScheme.primary),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),

                        ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.dp, vertical = if (isTablet) 32.dp else 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.notification_graphic),
                                    contentDescription = null,
                                    modifier = if (isTablet) Modifier
                                        .fillMaxWidth()
                                        .height(180.dp) else Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.Fit
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
                                    else -> 19.sp
                                }

                                val bodyFontSize = when {
                                    availableWidth > 1000.dp -> if (isTablet) 22.sp else 20.sp
                                    availableWidth > 800.dp -> if (isTablet) 22.sp else 17.sp
                                    else -> 16.sp
                                }


                                Column {
                                    Text(
                                        "Get Tips and Encouragement",
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = titleFontSize,
                                            fontWeight = FontWeight(750),
                                            letterSpacing = 0.9.sp,
//
                                        ),
                                        modifier = Modifier
                                            .padding(vertical = if (isTablet) 24.dp else 20.dp)
                                            .fillMaxWidth()
                                    )
                                    Text(
                                        "Stay equipped for conversations.\nAllow notifications today.",
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
                                        transitionState.targetState = false
                                    }
                                    // if hard denied:

                                },
                            ) {
                                Text(
                                    if (isHardDenied) "Notification Settings" else "Allow Notifications",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = if (isTablet) 22.sp else 17.sp)
                                )
                            }
                            TextButton(
                                modifier = Modifier
                                    .padding(bottom = 40.dp, top = 6.dp)
                                    .fillMaxWidth(), onClick = {

                                    transitionState.targetState = false

                                }) {
                                Text(
                                    "Maybe Later",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = if (isTablet) 22.sp else 17.sp)
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}
