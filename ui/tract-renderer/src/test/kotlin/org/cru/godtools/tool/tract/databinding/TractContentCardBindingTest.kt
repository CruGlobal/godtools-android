package org.cru.godtools.tool.tract.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.spyk
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.ccci.gto.android.common.testing.dagger.hilt.HiltTestActivity
import org.cru.godtools.shared.tool.parser.model.tips.Tip
import org.cru.godtools.shared.tool.parser.model.tract.CallToAction
import org.cru.godtools.shared.tool.parser.model.tract.TractPage
import org.cru.godtools.shared.tool.parser.model.tract.TractPage.Card
import org.cru.godtools.tool.tips.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class TractContentCardBindingTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var binding: TractContentCardBinding

    private val page = TractPage(
        cards = { listOf(spyk(Card(it))) },
        callToAction = { spyk(CallToAction(it)) }
    )
    private val card = page.cards.last()
    private val callToAction get() = page.callToAction
    private val tip = Tip(id = "tip")

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(HiltTestActivity::class.java).get()

        binding = TractContentCardBinding.inflate(LayoutInflater.from(activity), null, false)
    }

    private fun Card.setTips(vararg tips: Tip) = every { this@setTips.tips } returns listOf(*tips)
    private fun CallToAction.setTip(tip: Tip?) = every { this@setTip.tip } returns tip

    // region Tips Indicator
    @Test
    fun verifyTipsIndicatorHiddenWhenTipsDisabled() {
        card.setTips(tip)
        callToAction.setTip(tip)
        binding.enableTips = ImmutableLiveData(false)
        binding.model = card
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.tipsIndicator.visibility)
    }

    @Test
    fun verifyTipsIndicatorHiddenWhenCardAndCallToActionHaveNoTips() {
        binding.enableTips = ImmutableLiveData(true)
        binding.model = card
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.tipsIndicator.visibility)
    }

    @Test
    fun verifyTipsIndicatorVisibleWhenCardContainsTips() {
        card.setTips(tip)
        binding.enableTips = ImmutableLiveData(true)
        binding.model = card
        binding.executePendingBindings()

        assertEquals(View.VISIBLE, binding.tipsIndicator.visibility)
    }

    @Test
    fun verifyTipsIndicatorVisibleWhenCallToActionHasATip() {
        callToAction.setTip(tip)
        binding.enableTips = ImmutableLiveData(true)
        binding.model = card
        binding.executePendingBindings()

        assertEquals(View.VISIBLE, binding.tipsIndicator.visibility)
    }

    @Test
    fun verifyTipsIndicatorHiddenWhenCallToActionHasATipButThisIsntTheLastCard() {
        every { card.isLastVisibleCard } returns false
        callToAction.setTip(tip)
        binding.enableTips = ImmutableLiveData(true)
        binding.model = card
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.tipsIndicator.visibility)
    }

    @Test
    fun verifyTipIndicatorIconUsesFirstCardTip() {
        card.setTips(Tip(id = "ask", type = Tip.Type.ASK), Tip(id = "ask", type = Tip.Type.CONSIDER))
        callToAction.setTip(Tip(id = "quote", type = Tip.Type.QUOTE))
        binding.model = card
        binding.executePendingBindings()

        assertEquals(R.drawable.ic_tips_ask, Shadows.shadowOf(binding.tipsIndicator.drawable).createdFromResId)
    }

    @Test
    fun verifyTipIndicatorIconUsesCallToActionTip() {
        callToAction.setTip(Tip(id = "quote", type = Tip.Type.QUOTE))
        binding.model = card
        binding.executePendingBindings()

        assertEquals(R.drawable.ic_tips_quote, Shadows.shadowOf(binding.tipsIndicator.drawable).createdFromResId)
    }
    // endregion Tips Indicator
}
