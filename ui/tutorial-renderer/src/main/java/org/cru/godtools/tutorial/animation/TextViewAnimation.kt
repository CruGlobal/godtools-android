package org.cru.godtools.tutorial.animation

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView

fun AppCompatTextView.animateToNextText(@StringRes animateNextText: Int) {
    alpha = 1f
    val fadOutAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).apply {
        duration = fadeDuration
        setTextAfterAnimation(animateNextText, this@animateToNextText)
    }
    val fadeInAnimator = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f).apply {
        duration = fadeDuration
    }
    AnimatorSet().apply {
        startDelay = delayDuration
        play(fadOutAnimator).before(fadeInAnimator)
        start()
    }
}

private fun ObjectAnimator.setTextAfterAnimation(@StringRes animateNextText: Int, textView: AppCompatTextView) {
    addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {}

        override fun onAnimationEnd(animation: Animator?) {
            textView.setText(animateNextText)
        }

        override fun onAnimationCancel(animation: Animator?) {}

        override fun onAnimationStart(animation: Animator?) {}
    })
}

private const val delayDuration: Long = 3000
private const val fadeDuration = 1000L
