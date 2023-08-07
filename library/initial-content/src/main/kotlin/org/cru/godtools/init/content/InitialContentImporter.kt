package org.cru.godtools.init.content

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cru.godtools.init.content.task.Tasks

@Singleton
class InitialContentImporter @Inject internal constructor(tasks: Tasks) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        coroutineScope.launch {
            launch { tasks.loadBundledLanguages() }

            tasks.loadBundledResources()
            launch { tasks.initFavoriteTools() }
            launch { tasks.importBundledAttachments() }
            launch { tasks.importBundledTranslations() }
        }
    }
}
