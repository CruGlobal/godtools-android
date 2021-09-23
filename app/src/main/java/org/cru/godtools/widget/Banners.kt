package org.cru.godtools.widget

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.sergivonavi.materialbanner.Banner
import com.sergivonavi.materialbanner.BannerInterface
import org.cru.godtools.R

enum class BannerType(
    @StringRes internal val message: Int,
    @StringRes internal val primaryButton: Int,
    @StringRes internal val secondaryButton: Int? = null,
    @DrawableRes internal val icon: Int? = null
) {
    TOOL_LIST_FAVORITES(
        message = R.string.tools_list_favorites_banner_text,
        primaryButton = R.string.tools_list_favorites_banner_action_dismiss,
        icon = R.drawable.ic_favorite_24dp
    ),
    TUTORIAL_TRAINING(
        message = R.string.tutorial_features_banner_text,
        primaryButton = R.string.tutorial_features_banner_action_open,
        secondaryButton = R.string.tutorial_features_banner_action_dismiss
    );
}

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
    if (type.icon != null) {
        setIcon(type.icon)
        setIconTintColor(R.color.gt_blue)
    } else {
        setIcon(null)
    }
}

private val Banner.state
    get() = getTag(R.id.banner_state) as? BannerState ?: BannerState(this).also { setTag(R.id.banner_state, it) }

private class BannerState(private val banner: Banner) {
    var animate: Boolean = true

    var type: BannerType? = null
        set(value) {
            field = value
            updateDisplayedBanner()
        }
    var primaryCallback: BannerInterface.OnClickListener? = null
    var secondaryCallback: BannerInterface.OnClickListener? = null

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
