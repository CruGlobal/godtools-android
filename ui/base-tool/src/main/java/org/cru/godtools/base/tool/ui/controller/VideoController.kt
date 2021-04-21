package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.ccci.gto.android.common.androidx.lifecycle.onPause
import org.ccci.gto.android.common.androidx.lifecycle.onResume
import org.cru.godtools.base.tool.databinding.ToolContentVideoBinding
import org.cru.godtools.xml.model.Video

internal class VideoController private constructor(
    private val binding: ToolContentVideoBinding,
    parentController: BaseController<*>
) : BaseController<Video>(Video::class, binding.root, parentController) {
    @AssistedInject internal constructor(@Assisted parent: ViewGroup, @Assisted parentController: BaseController<*>) :
        this(ToolContentVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

    @AssistedFactory
    interface Factory : BaseController.Factory<VideoController>

    init {
        binding.controller = this
        lifecycleOwner?.lifecycle?.apply {
            onResume { binding.isVisible = true }
            onPause { binding.isVisible = false }
        }
    }

    public override fun onBind() {
        super.onBind()
        binding.model = model
    }
}
