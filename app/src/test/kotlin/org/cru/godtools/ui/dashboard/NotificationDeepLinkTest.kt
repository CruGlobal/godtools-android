package org.cru.godtools.ui.dashboard

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private const val HOST_GODTOOLS = "godtools.example.com"
private const val HOST_OTHER_APP = "other.example.com"

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class NotificationDeepLinkTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @BeforeTest
    fun setupActivities() {
        shadowOf(context.packageManager).apply {
            val godtoolsActivity = ComponentName(context, "org.cru.godtools.DeepLinkActivity")
            addActivityIfNotPresent(godtoolsActivity)
            addIntentFilterForActivity(godtoolsActivity, deepLinkIntentFilter(HOST_GODTOOLS))

            val otherAppActivity = ComponentName("com.example.other", "com.example.other.DeepLinkActivity")
            addActivityIfNotPresent(otherAppActivity)
            addIntentFilterForActivity(otherAppActivity, deepLinkIntentFilter(HOST_OTHER_APP))
        }
    }

    private fun deepLinkIntentFilter(host: String) = IntentFilter(Intent.ACTION_VIEW).apply {
        addCategory(Intent.CATEGORY_DEFAULT)
        addCategory(Intent.CATEGORY_BROWSABLE)
        addDataScheme("https")
        addDataAuthority(host, null)
    }

    // region createNotificationDeepLinkIntent()
    @Test
    fun `createNotificationDeepLinkIntent() - No deep link`() {
        assertNull(context.createNotificationDeepLinkIntent(Intent()))
        assertNull(context.createNotificationDeepLinkIntent(Intent().putExtra(EXTRA_NOTIFICATION_DEEP_LINK, "")))
    }

    @Test
    fun `createNotificationDeepLinkIntent() - Handled by GodTools`() {
        val deepLink = "https://$HOST_GODTOOLS/deeplink/path"
        val intent = Intent().putExtra(EXTRA_NOTIFICATION_DEEP_LINK, deepLink)

        val result = assertNotNull(context.createNotificationDeepLinkIntent(intent))
        assertEquals(Intent.ACTION_VIEW, result.action)
        assertEquals(Uri.parse(deepLink), result.data)
        assertEquals(context.packageName, result.`package`)
    }

    @Test
    fun `createNotificationDeepLinkIntent() - Only handled by another app`() {
        val intent = Intent().putExtra(EXTRA_NOTIFICATION_DEEP_LINK, "https://$HOST_OTHER_APP/deeplink/path")

        assertNull(context.createNotificationDeepLinkIntent(intent))
    }
    // endregion createNotificationDeepLinkIntent()
}
