package org.cru.godtools.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import org.cru.godtools.R

class OnBoardingActivity : AppCompatActivity(), OnBoardingCallbacks {

    // region lifecycle
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_onboarding)
    }

    // endregion lifecycle

    // region OnBoardingCallbacks
    override fun onNextClicked() {

    }

    override fun onPreviousClicked() {

    }

    override fun onCloseClicked() {
        finish()
    }
    // endregion OnBoardingCallbacks
}

interface OnBoardingCallbacks {
    fun onNextClicked()
    fun onPreviousClicked()
    fun onCloseClicked()
}

