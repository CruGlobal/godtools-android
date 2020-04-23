package org.cru.godtools.init.content

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cru.godtools.init.content.task.InitialContentTasks
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitialContentImporter @Inject internal constructor(context: Context) {
    init {
        GlobalScope.launch(Dispatchers.IO) {
            InitialContentTasks(context).run()
        }
    }
}
