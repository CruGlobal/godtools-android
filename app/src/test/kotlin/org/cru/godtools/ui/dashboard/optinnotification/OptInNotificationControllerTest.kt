package org.cru.godtools.ui.dashboard.optinnotification

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_OPT_IN_NOTIFICATION
import org.cru.godtools.ui.dashboard.DashboardActivity
import org.cru.godtools.ui.dashboard.DashboardViewModel
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class OptInNotificationControllerTest {
    private var permissionResult = PackageManager.PERMISSION_DENIED
    private var shouldShowRationale = false
    private var isFeatureDiscovered = true

    private val activity: DashboardActivity = mockk {
        every { checkPermission(Manifest.permission.POST_NOTIFICATIONS, any(), any()) } answers { permissionResult }
        every { shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) } answers {
            shouldShowRationale
        }
    }
    private val settings: Settings = mockk {
        every { isFeatureDiscovered(FEATURE_OPT_IN_NOTIFICATION) } answers { isFeatureDiscovered }
    }
    private val viewModel = DashboardViewModel()

    private val controller = OptInNotificationController(
        activity = activity,
        viewModel = viewModel,
        remoteConfig = mockk(),
        settings = settings,
    )

    // region init()
    @Test
    fun `init() - permission granted`() {
        permissionResult = PackageManager.PERMISSION_GRANTED

        controller.init()
        assertEquals(PermissionStatus.APPROVED, viewModel.permissionStatus)
    }

    @Test
    fun `init() - permission granted - feature not discovered`() {
        permissionResult = PackageManager.PERMISSION_GRANTED
        isFeatureDiscovered = false

        controller.init()
        assertEquals(PermissionStatus.APPROVED, viewModel.permissionStatus)
    }

    @Test
    fun `init() - permission denied - feature not discovered`() {
        isFeatureDiscovered = false

        controller.init()
        assertEquals(PermissionStatus.UNDETERMINED, viewModel.permissionStatus)
    }

    @Test
    fun `init() - permission denied - should show rationale`() {
        shouldShowRationale = true

        controller.init()
        assertEquals(PermissionStatus.SOFT_DENIED, viewModel.permissionStatus)
    }

    @Test
    fun `init() - permission denied - should not show rationale`() {
        controller.init()
        assertEquals(PermissionStatus.HARD_DENIED, viewModel.permissionStatus)
    }
    // endregion init()
}
