package org.cru.godtools.init.content

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cru.godtools.init.content.task.InitialContentTasks
import org.cru.godtools.init.content.task.Tasks
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitialContentImporter @Inject internal constructor(context: Context, tasks: Tasks) {
    init {
        GlobalScope.launch(Dispatchers.IO) {
            // languages init
            tasks.loadBundledLanguages()

            InitialContentTasks(context).run()
        }
    }
}
