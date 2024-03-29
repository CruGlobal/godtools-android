package org.cru.godtools.tutorial

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class TutorialActivityResultContract : ActivityResultContract<PageSet, Int>() {
    override fun createIntent(context: Context, input: PageSet) = context.buildTutorialActivityIntent(pageSet = input)
    override fun parseResult(resultCode: Int, intent: Intent?) = resultCode
}
