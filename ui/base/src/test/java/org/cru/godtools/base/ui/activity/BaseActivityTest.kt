package org.cru.godtools.base.ui.activity

import android.app.Application
import android.os.Handler
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.viewbinding.ViewBinding
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import org.ccci.gto.android.common.base.TimeConstants.DAY_IN_MS
import org.cru.godtools.base.ui.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = MockApplication::class)
class BaseActivityTest {
    lateinit var controller: ActivityController<ConcreteBaseActivity>
    lateinit var activity: BaseActivity<*>

    @Before
    fun setup() {
        controller = Robolectric.buildActivity(ConcreteBaseActivity::class.java)
        activity = controller.get()
    }

    @Test
    fun verifyFeatureDiscoveryHandlerDoesntLeakMessage() {
        ActivityScenario.launch(ConcreteBaseActivity::class.java).use { scenario ->
            lateinit var handler: Handler
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.onActivity { activity ->
                handler = activity.featureDiscoveryHandler
                activity.dispatchDelayedFeatureDiscovery("test", true, DAY_IN_MS)
            }
            assertTrue(handler.hasMessages(MSG_FEATURE_DISCOVERY))

            scenario.moveToState(Lifecycle.State.DESTROYED)
            assertFalse(handler.hasMessages(MSG_FEATURE_DISCOVERY))
        }
    }
}

class MockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setTheme(R.style.Theme_AppCompat)
    }
}

class ConcreteBaseActivity : BaseActivity<ViewBinding>() {
    init {
        settings = mock()
    }

    override fun inflateBinding() = mock<ViewBinding> {
        on { root } doAnswer { View(this@ConcreteBaseActivity) }
    }
}
