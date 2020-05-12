package org.cru.godtools.tract.viewmodel

import android.view.ViewGroup
import androidx.annotation.UiThread
import butterknife.BindView
import butterknife.OnClick
import org.ccci.gto.android.common.picasso.view.PicassoImageView
import org.cru.godtools.base.tool.model.view.ResourceViewUtils
import org.cru.godtools.tract.R
import org.cru.godtools.tract.R2
import org.cru.godtools.xml.model.Image
import org.cru.godtools.xml.model.resource

@UiThread
internal class ImageViewHolder(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
    BaseViewHolder<Image>(Image::class.java, parent, R.layout.tract_content_image, parentViewHolder) {
    @BindView(R2.id.image)
    lateinit var image: PicassoImageView

    public override fun onBind() {
        super.onBind()
        ResourceViewUtils.bind(mModel.resource, image)
    }

    @OnClick(R2.id.image)
    fun click() {
        mModel?.events?.let { sendEvents(it) }
    }
}
