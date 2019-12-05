package org.cru.godtools.tutorial.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView

fun AppCompatTextView.animateToNextText(@StringRes animateNextText: Int) {
    alpha = 1f
    animate().setDuration(3000).alpha(0f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            super.onAnimationEnd(animation)
            fadeInToNewText(animateNextText)
        }
    }).start()
}

private fun AppCompatTextView.fadeInToNewText(@StringRes newText: Int) {
    alpha = 0f
    setText(newText)
    animate().alpha(1f).setDuration(3000).setListener(null).start()
}
