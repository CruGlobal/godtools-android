package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import org.cru.godtools.base.model.Event
import org.cru.godtools.tract.databinding.TractContentAnimationBinding
import org.cru.godtools.xml.model.Animation

internal class AnimationController private constructor(
    private val binding: TractContentAnimationBinding,
    parentController: BaseController<*>?
) : BaseController<Animation>(Animation::class, binding.root, parentController) {
    internal constructor(parent: ViewGroup, parentViewHolder: BaseController<*>?) :
        this(TractContentAnimationBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentViewHolder)

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
