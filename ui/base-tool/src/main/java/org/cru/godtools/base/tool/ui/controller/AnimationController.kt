package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.databinding.ToolContentAnimationBinding
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.tool.model.Animation

internal class AnimationController private constructor(
    private val binding: ToolContentAnimationBinding,
    parentController: BaseController<*>
) : BaseController<Animation>(Animation::class, binding.root, parentController) {
    @AssistedInject internal constructor(@Assisted parent: ViewGroup, @Assisted parentController: BaseController<*>) :
        this(ToolContentAnimationBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

    @AssistedFactory
    interface Factory : BaseController.Factory<AnimationController>

    init {
        binding.controller = this
    }

    public override fun onBind() {
        super.onBind()
        binding.model = model
    }

    override fun onContentEvent(event: Event) {
        when {
            event.id in model?.playListeners.orEmpty() && !binding.animation.isAnimating ->
                binding.animation.resumeAnimation()
            event.id in model?.stopListeners.orEmpty() && binding.animation.isAnimating ->
                binding.animation.pauseAnimation()
        }
    }
}
