package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import org.cru.godtools.tract.databinding.TractContentTextBinding
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.xml.model.Text

internal class TextController private constructor(
    private val binding: TractContentTextBinding,
    parentViewHolder: BaseViewHolder<*>?
) : BaseViewHolder<Text>(binding.root, parentViewHolder) {
    internal constructor(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
        this(TractContentTextBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentViewHolder)

    public override fun onBind() {
        super.onBind()
        binding.model = model
    }
}
