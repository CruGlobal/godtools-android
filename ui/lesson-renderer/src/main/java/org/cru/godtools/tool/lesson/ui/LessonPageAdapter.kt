package org.cru.godtools.tool.lesson.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.ccci.gto.android.common.androidx.viewpager2.adapter.PrimaryItemChangeObserver
import org.ccci.gto.android.common.androidx.viewpager2.adapter.onUpdatePrimaryItem
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.ccci.gto.android.common.support.v4.util.IdUtils
import org.cru.godtools.tool.lesson.databinding.LessonPageBinding
import org.cru.godtools.tool.lesson.ui.controller.LessonPageController
import org.cru.godtools.tool.lesson.ui.controller.bindController
import org.cru.godtools.tool.model.lesson.LessonPage

class LessonPageAdapter @AssistedInject internal constructor(
    @Assisted lifecycleOwner: LifecycleOwner,
    @Assisted private val callbacks: Callbacks?,
    private val controllerFactory: LessonPageController.Factory
) : SimpleDataBindingAdapter<LessonPageBinding>(lifecycleOwner) {
    @AssistedFactory
    interface Factory {
        fun create(lifecycleOwner: LifecycleOwner, callbacks: Callbacks?): LessonPageAdapter
    }

    interface Callbacks {
        fun goToPreviousPage()
        fun goToNextPage()
    }

    init {
        setHasStableIds(true)
    }

    var pages: List<LessonPage> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var primaryItemObserver: PrimaryItemChangeObserver<*>? = null

    override fun getItemCount() = pages.size
    private fun getItem(position: Int) = pages[position]
    override fun getItemId(position: Int) = IdUtils.convertId(getItem(position).id)

    // region Lifecycle
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        primaryItemObserver = onUpdatePrimaryItem(recyclerView) { primary, previous ->
            previous?.binding?.controller?.lifecycleOwner?.apply { maxState = minOf(maxState, Lifecycle.State.STARTED) }
            primary?.binding?.controller?.lifecycleOwner?.apply { maxState = maxOf(maxState, Lifecycle.State.RESUMED) }
        }
    }

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        LessonPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun onViewDataBindingCreated(binding: LessonPageBinding, viewType: Int) {
        binding.callbacks = callbacks
        binding.bindController(controllerFactory)
    }

    override fun onBindViewDataBinding(binding: LessonPageBinding, position: Int) {
        binding.controller?.apply {
            model = getItem(position)
            lifecycleOwner?.apply { maxState = maxOf(maxState, Lifecycle.State.STARTED) }
        }
        binding.isFirstPage = position == 0
        binding.isLastPage = position == pages.size - 1
    }

    override fun onViewDataBindingRecycled(binding: LessonPageBinding) {
        binding.controller?.lifecycleOwner?.apply { maxState = minOf(maxState, Lifecycle.State.CREATED) }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        primaryItemObserver?.unregister()
        primaryItemObserver = null
    }
    // endregion Lifecycle
}
