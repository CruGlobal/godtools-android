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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.R

class OptInNotificationModalOverlay(
    val requestPermission: suspend () -> Unit,
) : Overlay<Unit> {

    @Composable
    override fun Content(navigator: OverlayNavigator<Unit>) {
        BackHandler { navigator.finish(Unit) }


        val coroutineScope = rememberCoroutineScope()
        val transitionState = remember { MutableTransitionState(false) }

        LaunchedEffect(Unit) { transitionState.targetState = true }

        LaunchedEffect(transitionState.currentState, transitionState.targetState) {
            if (!transitionState.currentState && transitionState.isIdle) {
                navigator.finish(Unit)
            }
        }

        // TODO - DSR: Adaptive sizing for tablets
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
                Box {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
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
                                .padding(horizontal = 28.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.notification_graphic),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.Fit
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.primary,
                                    thickness = 2.4.dp,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                )
                            }

                            Text(
                                "Get Tips and Encouragement",
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                // TODO - DSR: ensure text never overflows to multiple lines
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 23.sp, fontWeight = FontWeight(750), letterSpacing = 0.9.sp
                                ),
                                modifier = Modifier.padding(vertical = 18.dp)
                            )
                            Text(
                                "Stay equipped for conversations.\nAllow notifications today.",
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight(weight = 420),
                                    letterSpacing = 0.9.sp,
                                    lineHeight = 24.sp
                                ),
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

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

                                }) {
                                Text(
                                    "Allow Notifications",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
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
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}
