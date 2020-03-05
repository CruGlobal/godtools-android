package org.cru.godtools.xml.service

import android.content.Context
import org.keynote.godtools.android.db.GodToolsDao

open class KotlinManifestManager(@JvmField protected val context: Context) {
    @JvmField
    protected val dao = GodToolsDao.getInstance(context)
}
