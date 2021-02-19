package org.cru.godtools.tool.lesson.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.tool.lesson.databinding.LessonPageBinding
import org.cru.godtools.tool.lesson.ui.controller.LessonPageController
import org.cru.godtools.tool.lesson.ui.controller.bindController
import org.cru.godtools.xml.model.Manifest

class LessonPageAdapter @AssistedInject internal constructor(
    @Assisted lifecycleOwner: LifecycleOwner,
    private val controllerFactory: LessonPageController.Factory
) : SimpleDataBindingAdapter<LessonPageBinding>(lifecycleOwner), Observer<Manifest?> {
    @AssistedFactory
    interface Factory {
        fun create(lifecycleOwner: LifecycleOwner): LessonPageAdapter
    }

    var manifest: Manifest? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = manifest?.lessonPages?.size ?: 0
    private fun getItem(position: Int) = manifest?.lessonPages?.getOrNull(position)

    // region Lifecycle
    override fun onChanged(t: Manifest?) {
        manifest = t
    }

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        LessonPageBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
            bindController(controllerFactory)
        }

    override fun onBindViewDataBinding(binding: LessonPageBinding, position: Int) {
        binding.controller?.model = getItem(position)
    }
    // endregion Lifecycle
}
