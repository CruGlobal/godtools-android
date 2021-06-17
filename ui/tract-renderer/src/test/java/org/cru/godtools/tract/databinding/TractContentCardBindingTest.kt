package org.cru.godtools.tract.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.ccci.gto.android.common.testing.dagger.hilt.HiltTestActivity
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.tips.Tip
import org.cru.godtools.tool.model.tract.CallToAction
import org.cru.godtools.tool.model.tract.Card
import org.cru.godtools.tool.model.tract.TractPage
import org.cru.godtools.tract.R
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

    private lateinit var page: TractPage
    private val card get() = page.cards[0]
    private val callToAction get() = page.callToAction
    private lateinit var tip: Tip

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(HiltTestActivity::class.java).get()

        binding = TractContentCardBinding.inflate(LayoutInflater.from(activity), null, false)

        tip = Tip(id = "tip")
        page = TractPage(Manifest(), cards = { listOf(spy(Card(it))) }, callToAction = { spy(CallToAction(it)) })
    }

    // region Tips Indicator
    @Test
    fun verifyTipsIndicatorHiddenWhenTipsDisabled() {
        card.setTips(tip)
        callToAction.setTip(tip)
        binding.enableTips = false
        binding.model = card
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.tipsIndicator.visibility)
    }

    @Test
    fun verifyTipsIndicatorHiddenWhenCardAndCallToActionHaveNoTips() {
        binding.enableTips = true
        binding.model = card
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.tipsIndicator.visibility)
    }

    @Test
    fun verifyTipsIndicatorVisibleWhenCardContainsTips() {
        card.setTips(tip)
        binding.enableTips = true
        binding.model = card
        binding.executePendingBindings()

        assertEquals(View.VISIBLE, binding.tipsIndicator.visibility)
    }

    @Test
    fun verifyTipsIndicatorVisibleWhenCallToActionHasATip() {
        callToAction.setTip(tip)
        binding.enableTips = true
        binding.model = card
        binding.executePendingBindings()

        assertEquals(View.VISIBLE, binding.tipsIndicator.visibility)
    }

    @Test
    fun verifyTipsIndicatorHiddenWhenCallToActionHasATipButThisIsntTheLastCard() {
        whenever(card.isLastVisibleCard).thenReturn(false)
        callToAction.setTip(tip)
        binding.enableTips = true
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

private fun Card.setTips(vararg tips: Tip) = whenever(this.tips).thenReturn(listOf(*tips))
private fun CallToAction.setTip(tip: Tip?) = whenever(this.tip).thenReturn(tip)
