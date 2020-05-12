package org.cru.godtools.tract.viewmodel

import android.view.ViewGroup
import androidx.annotation.UiThread
import org.cru.godtools.tract.R
import org.cru.godtools.tract.databinding.TractContentTextBinding
import org.cru.godtools.xml.model.Text

@UiThread
internal class TextViewHolder(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
    BaseViewHolder<Text>(Text::class.java, parent, R.layout.tract_content_text, parentViewHolder) {
    private val binding = TractContentTextBinding.bind(mRoot)

    public override fun onBind() {
        super.onBind()
        binding.model = mModel
    }
}
