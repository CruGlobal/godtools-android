@file:JvmName("Banners")
package org.cru.godtools.widget

import android.view.View
import androidx.annotation.StringRes
import com.sergivonavi.materialbanner.Banner
import com.sergivonavi.materialbanner.BannerInterface
import org.cru.godtools.R

enum class BannerType(
    @StringRes internal val message: Int,
    @StringRes internal val primaryButton: Int,
    @StringRes internal val secondaryButton: Int? = null
) {
    TUTORIAL_TRAINING(
        R.string.tutorial_training_banner_text,
        R.string.tutorial_training_banner_action_open,
        R.string.tutorial_training_banner_action_dismiss
    );
}

@JvmOverloads
fun Banner.show(
    banner: BannerType? = null,
    primaryCallback: BannerInterface.OnClickListener? = null,
    secondaryCallback: BannerInterface.OnClickListener? = null,
    animate: Boolean = true
) {
    val state = state
    state.animate = animate
    state.primaryCallback = primaryCallback
    state.secondaryCallback = secondaryCallback
    state.type = banner
}

private fun Banner.updateUi(
    type: BannerType?,
    primaryCallback: BannerInterface.OnClickListener? = null,
    secondaryCallback: BannerInterface.OnClickListener? = null
) {
    if (type == null) return

    setMessage(type.message)
    setRightButton(type.primaryButton, primaryCallback)
    if (type.secondaryButton != null) {
        setLeftButton(type.secondaryButton, secondaryCallback)
    } else {
        setLeftButton(null, null)
    }
}

private var Banner.state: BannerState
    get() = getTag(R.id.banner_state) as? BannerState ?: BannerState(this).also { state = it }
    set(value) = setTag(R.id.banner_state, value)

private class BannerState(private val banner: Banner) {
    internal var animate: Boolean = true

    internal var type: BannerType? = null
        set(value) {
            field = value
            updateDisplayedBanner()
        }
    internal var primaryCallback: BannerInterface.OnClickListener? = null
    internal var secondaryCallback: BannerInterface.OnClickListener? = null

    private var visibleType: BannerType? = null
        set(value) {
            field = value
            banner.updateUi(value, primaryCallback, secondaryCallback)
        }

    // region Banner Animation
    private var isAnimating = false

    private fun updateDisplayedBanner() = when {
        isAnimating -> Unit
        type == visibleType -> Unit
        visibleType != null -> {
            if (animate) {
                isAnimating = true
                banner.dismiss()
            } else banner.setBannerVisibility(View.GONE)
        }
        type != null -> {
            if (animate) {
                isAnimating = true
                visibleType = type
                banner.show()
            } else {
                visibleType = type
                banner.setBannerVisibility(View.VISIBLE)
            }
        }
        else -> Unit
    }

    init {
        banner.setOnShowListener {
            isAnimating = false
            updateDisplayedBanner()
        }
    }

    init {
        banner.setOnDismissListener {
            isAnimating = false
            visibleType = null
            updateDisplayedBanner()
        }
    }
    // endregion Banner Animation
}
