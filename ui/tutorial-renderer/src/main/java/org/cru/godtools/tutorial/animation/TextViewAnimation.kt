package org.cru.godtools.tutorial.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.widget.TextView
import androidx.annotation.StringRes

private const val DELAY_DURATION = 3000L
private const val FADE_DURATION = 1000L

fun TextView.animateToNextText(@StringRes animateNextText: Int) {
    alpha = 1f
    val fadeOutAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).apply {
        duration = FADE_DURATION
        setTextAfterAnimation(animateNextText, this@animateToNextText)
    }
    val fadeInAnimator = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f).apply {
        duration = FADE_DURATION
    }
    AnimatorSet().apply {
        startDelay = DELAY_DURATION
        play(fadeOutAnimator).before(fadeInAnimator)
        start()
    }
}

private fun ObjectAnimator.setTextAfterAnimation(@StringRes animateNextText: Int, textView: TextView) {
    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            textView.setText(animateNextText)
        }
    })
}
