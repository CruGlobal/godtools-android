package org.cru.godtools.tutorial.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import org.cru.godtools.tutorial.databinding.TutorialOnboardingWelcomeBinding

private const val WELCOME_FADE_DELAY = 2000L
private const val WELCOME_FADE_OUT_DURATION = 600L
private const val WELCOME_FADE_IN_DURATION = 600L

internal fun TutorialOnboardingWelcomeBinding.animateViews() {
    welcomeTextAnimator().start()
}

private fun TutorialOnboardingWelcomeBinding.welcomeTextAnimator() = AnimatorSet().apply {
    startDelay = WELCOME_FADE_DELAY

    val fadeOut = ObjectAnimator.ofFloat(welcome, "alpha", 1f, 0f).apply {
        duration = WELCOME_FADE_OUT_DURATION
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                welcome.visibility = View.INVISIBLE
            }
        })
    }
    val fadeIn = AnimatorSet().apply {
        playTogether(
            ObjectAnimator.ofFloat(actionOnboardingWatchVideo, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(welcome2, "alpha", 0f, 1f)
        )
        duration = WELCOME_FADE_IN_DURATION
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                actionOnboardingWatchVideo.visibility = View.VISIBLE
                welcome2.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator?) {
                actionOnboardingWatchVideo.alpha = 1f
                welcome2.alpha = 1f
            }
        })
    }

    play(fadeOut).before(fadeIn)
}
