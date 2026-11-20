package org.cru.godtools.ui.account

import androidx.compose.foundation.pager.rememberPagerState
import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.time.Instant
import java.time.Year
import kotlin.test.Test
import kotlinx.collections.immutable.toImmutableList
import org.cru.godtools.base.ui.BasePaparazziTest
import org.cru.godtools.model.GlobalActivityAnalytics
import org.cru.godtools.model.User
import org.cru.godtools.shared.user.activity.UserCounterNames
import org.cru.godtools.shared.user.activity.model.UserActivity
import org.cru.godtools.ui.account.globalactivity.GlobalActivityScreen
import org.cru.godtools.ui.drawer.DrawerMenuScreenStateTestData
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class AccountLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    private val state = AccountPresenter.UiState(
        user = User(
            id = "user",
            createdAt = Instant.parse("2023-04-14T12:00:00Z"),
            name = "Test User",
        ),
        pages = AccountPage.entries.toImmutableList(),
        userActivity = UserActivity(
            buildMap {
                repeat(6) { put(UserCounterNames.TOOL_OPEN("tool$it"), it + 1) }
                repeat(4) { put("lesson_completions.$it", it + 1) }
                repeat(7) { put(UserCounterNames.ARTICLE_OPEN("example:$it"), it + 1) }
                put(UserCounterNames.IMAGE_SHARED, 7)
                put(UserCounterNames.TIPS_COMPLETED, 8)
            }
        ),
        globalActivity = GlobalActivityScreen.UiState(
            year = Year.of(2025),
            activity = GlobalActivityAnalytics(
                users = 1234,
                gospelPresentations = 4321,
                countries = 123,
                launches = 54321,
            ),
        ),
        drawerState = DrawerMenuScreenStateTestData.closed,
    )

    @Test
    fun `AccountLayout()`() = snapshot { AccountLayout(state) }

    @Test
    fun `AccountLayout() - Global Activity tab selected`() = snapshot {
        AccountLayout(state, pagerState = rememberPagerState(initialPage = 1) { state.pages.size })
    }

    @Test
    fun `AccountLayout() - Drawer Open`() = snapshot {
        AccountLayout(state.copy(drawerState = DrawerMenuScreenStateTestData.open))
    }

    @Test
    fun `AccountLayout() - No user`() = snapshot {
        AccountLayout(state.copy(user = null))
    }
}
