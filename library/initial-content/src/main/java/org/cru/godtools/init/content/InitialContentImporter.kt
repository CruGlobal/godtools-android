package org.cru.godtools.init.content

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.cru.godtools.init.content.task.Tasks

@Singleton
class InitialContentImporter @Inject internal constructor(tasks: Tasks) {
    init {
        GlobalScope.launch(Dispatchers.IO) {
            val languages = async {
                tasks.loadBundledLanguages()
                tasks.initSystemLanguages()
            }

            tasks.loadBundledTools()
            launch { tasks.importBundledAttachments() }
            tasks.initDefaultTools()

            languages.await()
            tasks.importBundledTranslations()
        }
    }
}
