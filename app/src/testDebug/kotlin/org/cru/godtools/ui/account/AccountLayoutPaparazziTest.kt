package org.cru.godtools.ui.account

import androidx.compose.foundation.pager.rememberPagerState
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.time.Instant
import kotlin.test.Test
import org.cru.godtools.base.ui.BasePaparazziTest
import org.cru.godtools.model.User
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class AccountLayoutPaparazziTest(
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(nightMode = nightMode, accessibilityMode = accessibilityMode) {
    private val user = User(
        id = "user",
        createdAt = Instant.parse("2023-04-14T12:00:00Z"),
        name = "Test User",
    )
    private val pages = AccountPage.entries.toList()

    @Test
    fun `AccountLayoutHeader()`() {
        snapshot { AccountLayoutHeader(user = user, pages = pages) }
    }

    @Test
    fun `AccountLayoutHeader() - Global Activity tab selected`() {
        snapshot {
            AccountLayoutHeader(
                user = user,
                pages = pages,
                pagerState = rememberPagerState(initialPage = 1) { pages.size },
            )
        }
    }
}
