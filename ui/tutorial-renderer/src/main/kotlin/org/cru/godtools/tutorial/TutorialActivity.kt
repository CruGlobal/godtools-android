package org.cru.godtools.tutorial

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuitx.android.IntentScreen
import com.slack.circuitx.android.rememberAndroidScreenAwareNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.ccci.gto.android.common.compat.content.getSerializableExtraCompat

private const val ARG_PAGE_SET = "pageSet"

fun Context.buildTutorialActivityIntent(pageSet: PageSet) = Intent(this, TutorialActivity::class.java)
    .putExtra(ARG_PAGE_SET, pageSet)

fun Context.startTutorialActivity(pageSet: PageSet) = startActivity(buildTutorialActivityIntent(pageSet))

@AndroidEntryPoint
class TutorialActivity : AppCompatActivity() {
    companion object {
        const val RESULT_SHOW_QR_CODE = 10
    }

    private val pageSet get() = intent?.getSerializableExtraCompat(ARG_PAGE_SET, PageSet::class.java) ?: PageSet.DEFAULT

    @Inject
    internal lateinit var circuit: Circuit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CircuitCompositionLocals(circuit) {
                val backStack = rememberSaveableBackStack(TutorialScreen(pageSet))
                val navigator = rememberAndroidScreenAwareNavigator(
                    rememberCircuitNavigator(backStack) { result ->
                        setResult(
                            when (result) {
                                null -> RESULT_CANCELED
                                is TutorialScreen.Result -> result.resultCode
                                else -> RESULT_OK
                            }
                        )
                        finish()
                    },
                ) { screen ->
                    when (screen) {
                        is IntentScreen -> screen.startWith(this)

                        is YoutubePlayerScreen -> {
                            startYoutubePlayerActivity(screen.videoId)
                            true
                        }

                        else -> false
                    }
                }
                CircuitContent(TutorialScreen(pageSet), navigator)
            }
        }
    }
}
