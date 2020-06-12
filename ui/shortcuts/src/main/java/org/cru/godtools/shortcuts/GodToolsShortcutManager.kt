package org.cru.godtools.shortcuts

import android.content.Context
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService

open class KotlinGodToolsShortcutManager(protected val context: Context) {
    @get:RequiresApi(Build.VERSION_CODES.N_MR1)
    protected val shortcutManager by lazy { context.getSystemService<ShortcutManager>() }
}
