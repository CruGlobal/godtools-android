package org.cru.godtools.ui.account.activity

import android.net.Uri
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import org.cru.godtools.shared.user.activity.UserCounterNames
import org.cru.godtools.shared.user.activity.model.UserActivity
import org.cru.godtools.ui.BasePaparazziTest
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class AccountActivityBadgesPaparazziTest(@TestParameter nightMode: NightMode) :
    BasePaparazziTest(nightMode = nightMode, renderingMode = RenderingMode.SHRINK) {
    @Test
    fun `AccountActivityBadges() - All Incomplete`() {
        val activity = UserActivity(emptyMap())

        centerInSnapshot { AccountActivityBadges(activity) }
    }

    @Test
    fun `AccountActivityBadges() - Some Complete`() {
        val activity = UserActivity(
            buildMap {
                repeat(6) { put(UserCounterNames.TOOL_OPEN("tool$it"), it + 1) }
                repeat(4) { put("lesson_completions.$it", it + 1) }
                repeat(7) { put(UserCounterNames.ARTICLE_OPEN(Uri.parse("example:$it")), it + 1) }
                put(UserCounterNames.IMAGE_SHARED, 7)
                put(UserCounterNames.TIPS_COMPLETED, 8)
            }
        )

        centerInSnapshot { AccountActivityBadges(activity) }
    }

    @Test
    fun `AccountActivityBadges() - All Complete`() {
        val activity = UserActivity(
            buildMap {
                repeat(10) { put(UserCounterNames.TOOL_OPEN("tool$it"), it + 1) }
                repeat(10) { put("lesson_completions.$it", it + 1) }
                repeat(10) { put(UserCounterNames.ARTICLE_OPEN(Uri.parse("example:$it")), it + 1) }
                put(UserCounterNames.IMAGE_SHARED, 10)
                put(UserCounterNames.TIPS_COMPLETED, 20)
            }
        )

        centerInSnapshot { AccountActivityBadges(activity) }
    }
}
