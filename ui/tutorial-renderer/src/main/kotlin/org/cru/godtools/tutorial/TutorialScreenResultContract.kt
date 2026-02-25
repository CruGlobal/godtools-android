package org.cru.godtools.tutorial

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import org.ccci.gto.android.common.compat.content.getParcelableExtraCompat
import org.cru.godtools.base.ui.circuit.CircuitActivity
import org.cru.godtools.base.ui.circuit.createCircuitActivityIntent
import org.cru.godtools.tutorial.layout.TutorialScreen

class TutorialScreenResultContract : ActivityResultContract<PageSet, TutorialScreen.Result?>() {
    override fun createIntent(context: Context, input: PageSet) =
        context.createCircuitActivityIntent(TutorialScreen(input))

    override fun parseResult(resultCode: Int, intent: Intent?) =
        intent?.getParcelableExtraCompat(CircuitActivity.EXTRA_RESULT, TutorialScreen.Result::class.java)
}
