package org.cru.godtools.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import org.ccci.gto.android.common.picasso.view.PicassoImageView
import org.cru.godtools.model.Attachment
import org.cru.godtools.util.bindLocalImage

@BindingAdapter("android:src")
fun ImageView?.bindAttachment(attachment: Attachment?) {
    (this as? PicassoImageView)?.bindLocalImage(attachment)
}
