package org.cru.godtools.tract.viewmodel

import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.UiThread
import butterknife.BindView
import org.cru.godtools.base.tool.model.view.bindTo
import org.cru.godtools.tract.R
import org.cru.godtools.tract.R2
import org.cru.godtools.xml.model.Text

@UiThread
internal class TextViewHolder(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
    BaseViewHolder<Text>(Text::class.java, parent, R.layout.tract_content_text, parentViewHolder) {
    @BindView(R2.id.content_text)
    lateinit var text: TextView

    public override fun onBind() {
        super.onBind()
        mModel.bindTo(text)
    }
}
