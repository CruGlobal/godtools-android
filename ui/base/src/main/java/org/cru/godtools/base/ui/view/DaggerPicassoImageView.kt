package org.cru.godtools.base.ui.view

import android.content.Context
import android.util.AttributeSet
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.ccci.gto.android.common.picasso.view.PicassoImageView
import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView

@AndroidEntryPoint
class DaggerPicassoImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SimplePicassoImageView(context, attrs, defStyleAttr) {
    @Inject
    internal lateinit var picasso: Picasso

    override val helper = PicassoImageView.Helper(this, attrs, defStyleAttr, picasso = picasso)
}
