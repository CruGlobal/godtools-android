package org.cru.godtools.content

import android.content.Context
import android.os.Bundle

import org.ccci.gto.android.common.eventbus.content.DaoCursorEventBusLoader
import org.cru.godtools.model.Tool
import org.cru.godtools.model.event.content.ToolEventBusSubscriber
import org.keynote.godtools.android.db.GodToolsDao

open class ToolsCursorLoader(context: Context, args: Bundle?) :
    DaoCursorEventBusLoader<Tool>(context, GodToolsDao.getInstance(context), Tool::class.java, args) {
    init {
        addEventBusSubscriber(ToolEventBusSubscriber(this))
    }
}
