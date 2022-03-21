package org.cru.godtools.tract.analytics.appsflyer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.tract.activity.TractActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TractAppsFlyerDeepLinkResolverTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun verifyResolveNoUri() {
        assertNull(TractAppsFlyerDeepLinkResolver.resolve(context, null, emptyMap()))
    }

    @Test
    fun verifyResolveInvalidUri() {
        assertNull(TractAppsFlyerDeepLinkResolver.resolve(context, Uri.parse("https://example.com/en/kgp"), emptyMap()))
    }

    @Test
    fun verifyResolveValidUri() {
        val uri = Uri.parse("https://knowgod.com/en/kgp")

        val intent = TractAppsFlyerDeepLinkResolver.resolve(context, uri, emptyMap())!!
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(uri, intent.data)
        assertEquals(ComponentName(context, TractActivity::class.java), intent.component)
    }
}
