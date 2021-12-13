package org.cru.godtools.tool.cyoa.databinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.ccci.gto.android.common.androidx.viewpager2.widget.currentItemLiveData
import org.ccci.gto.android.common.testing.dagger.hilt.HiltTestActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class CyoaPageCardCollectionBindingTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var binding: CyoaPageCardCollectionBinding
    private val cardAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var items = 3
            set(value) {
                field = value
                notifyDataSetChanged()
            }
        override fun getItemCount() = items
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            object : RecyclerView.ViewHolder(View(parent.context)) {}
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = Unit
    }

    @Before
    fun setupBinding() {
        val activity = Robolectric.buildActivity(HiltTestActivity::class.java).get()
        binding = CyoaPageCardCollectionBinding.inflate(LayoutInflater.from(activity))
        binding.lifecycleOwner = TestLifecycleOwner()
        binding.cards.adapter = cardAdapter
        binding.currentCardIndex = binding.cards.currentItemLiveData
        binding.executePendingBindings()
    }

    // region action_next
    @Test
    fun `action_next - click`() {
        cardAdapter.items = 2
        assertEquals(0, binding.cards.currentItem)

        binding.actionNext.performClick()
        assertEquals(1, binding.cards.currentItem)
    }

    @Test
    fun `action_next - visibility`() {
        cardAdapter.items = 2

        binding.cards.currentItem = 0
        binding.executePendingBindings()
        assertEquals(View.VISIBLE, binding.actionNext.visibility)

        binding.cards.currentItem = 1
        binding.executePendingBindings()
        assertEquals(View.GONE, binding.actionNext.visibility)
    }
    // endregion action_next

    // region action_prev
    @Test
    fun `action_prev - click`() {
        cardAdapter.items = 2
        binding.cards.currentItem = 1
        assertEquals(1, binding.cards.currentItem)

        binding.actionPrev.performClick()
        assertEquals(0, binding.cards.currentItem)
    }

    @Test
    fun `action_prev - visibility`() {
        cardAdapter.items = 2

        binding.cards.currentItem = 0
        binding.executePendingBindings()
        assertEquals(View.GONE, binding.actionPrev.visibility)

        binding.cards.currentItem = 1
        binding.executePendingBindings()
        assertEquals(View.VISIBLE, binding.actionPrev.visibility)
    }
    // endregion action_prev
}
