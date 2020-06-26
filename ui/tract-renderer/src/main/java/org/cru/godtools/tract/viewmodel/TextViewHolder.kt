package org.cru.godtools.tract.viewmodel

import android.view.ViewGroup
import org.cru.godtools.tract.R
import org.cru.godtools.tract.databinding.TractContentTextBinding
import org.cru.godtools.xml.model.Text

internal class TextViewHolder(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
    BaseViewHolder<Text>(parent, R.layout.tract_content_text, parentViewHolder) {
    private val binding = TractContentTextBinding.bind(mRoot)

    public override fun onBind() {
        super.onBind()
        binding.model = mModel
    }
}
