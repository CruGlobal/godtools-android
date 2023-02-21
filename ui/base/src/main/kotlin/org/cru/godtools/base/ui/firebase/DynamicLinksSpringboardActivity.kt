package org.cru.godtools.base.ui.firebase

import android.content.ComponentName
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.cru.godtools.base.ui.startDashboardActivity
import timber.log.Timber

private const val TAG = "DynamicLinks"

@AndroidEntryPoint
class DynamicLinksSpringboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDynamicLinks()
    }

    private fun handleDynamicLinks() {
        lifecycleScope.launch {
            try {
                val link = Firebase.dynamicLinks.getDynamicLink(intent).await()
                link?.link?.let { uri ->
                    val intent = Intent(ACTION_VIEW, uri)

                    // only support launching the link in this app
                    val activityInfo = packageManager.queryIntentActivities(intent, 0)
                        .firstOrNull { it.activityInfo.packageName == packageName }
                        ?.activityInfo
                    if (activityInfo != null) {
                        intent.component = ComponentName(activityInfo.packageName, activityInfo.name)
                        startActivity(intent)
                        return@launch
                    }
                }
                startDashboardActivity()
            } catch (e: CancellationException) {
                startDashboardActivity()
                throw e
            } catch (e: Exception) {
                Timber.tag(TAG).e("Unhandled error retrieving dynamic link")
                startDashboardActivity()
            } finally {
                finish()
            }
        }
    }
}
