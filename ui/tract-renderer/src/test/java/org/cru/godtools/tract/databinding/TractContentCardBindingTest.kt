package org.cru.godtools.tract.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import com.squareup.picasso.picassoSingleton
import org.cru.godtools.tract.R
import org.cru.godtools.xml.model.CallToAction
import org.cru.godtools.xml.model.Card
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Page
import org.cru.godtools.xml.model.tips.Tip
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class TractContentCardBindingTest {
    private lateinit var binding: TractContentCardBinding

    private lateinit var page: Page
    private val card get() = page.cards[0]
    private val callToAction get() = page.callToAction
    private lateinit var tip: Tip

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        val context = ContextThemeWrapper(activity, R.style.Theme_AppCompat)
        picassoSingleton = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)

        binding = TractContentCardBinding.inflate(LayoutInflater.from(context), null, false)

        tip = Tip(id = "tip")
        page = Page(Manifest(), cards = { listOf(spy(Card(it))) }, callToAction = { spy(CallToAction(it)) })
    }

    @After
    fun cleanup() {
        picassoSingleton = null
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
    // endregion Tips Indicator
}

private fun Card.setTips(vararg tips: Tip) = whenever(this.tips).thenReturn(listOf(*tips))
private fun CallToAction.setTip(tip: Tip?) = whenever(this.tip).thenReturn(tip)
