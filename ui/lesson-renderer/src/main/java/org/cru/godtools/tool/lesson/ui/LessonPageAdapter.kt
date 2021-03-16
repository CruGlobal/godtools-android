package org.cru.godtools.tool.lesson.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.ccci.gto.android.common.support.v4.util.IdUtils
import org.cru.godtools.tool.lesson.databinding.LessonPageBinding
import org.cru.godtools.tool.lesson.ui.controller.LessonPageController
import org.cru.godtools.tool.lesson.ui.controller.bindController
import org.cru.godtools.xml.model.lesson.LessonPage

class LessonPageAdapter @AssistedInject internal constructor(
    @Assisted lifecycleOwner: LifecycleOwner,
    private val controllerFactory: LessonPageController.Factory
) : SimpleDataBindingAdapter<LessonPageBinding>(lifecycleOwner) {
    @AssistedFactory
    interface Factory {
        fun create(lifecycleOwner: LifecycleOwner): LessonPageAdapter
    }

    interface Callbacks {
        fun goToPreviousPage()
        fun goToNextPage()
    }

    init {
        setHasStableIds(true)
    }

    private val _callbacks = ObservableField<Callbacks?>()
    var callbacks
        get() = _callbacks.get()
        set(value) = _callbacks.set(value)

    var pages: List<LessonPage> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = pages.size
    private fun getItem(position: Int) = pages[position]
    override fun getItemId(position: Int) = IdUtils.convertId(getItem(position).id)

    // region Lifecycle
    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        LessonPageBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
            callbacks = _callbacks
            bindController(controllerFactory)
        }

    override fun onBindViewDataBinding(binding: LessonPageBinding, position: Int) {
        binding.controller?.model = getItem(position)
        binding.isFirstPage = position == 0
        binding.isLastPage = position == pages.size - 1
    }
    // endregion Lifecycle
}
