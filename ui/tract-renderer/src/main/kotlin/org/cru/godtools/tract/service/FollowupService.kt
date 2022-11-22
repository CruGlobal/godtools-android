package org.cru.godtools.tract.service

import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.dagger.getValue
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.db.repository.FollowupsRepository
import org.cru.godtools.model.Followup
import org.cru.godtools.shared.tool.parser.model.EventId
import org.cru.godtools.sync.GodToolsSyncService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

private const val FIELD_NAME = "name"
private const val FIELD_EMAIL = "email"
private const val FIELD_DESTINATION = "destination_id"

@Singleton
class FollowupService @Inject internal constructor(
    eventBus: EventBus,
    followupsRepository: Lazy<FollowupsRepository>,
    syncService: Lazy<GodToolsSyncService>
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val followupsRepository by followupsRepository
    private val syncService by syncService

    @Subscribe
    fun onContentEvent(event: Event) {
        if (event.id != EventId.FOLLOWUP) return

        coroutineScope.launch {
            val followup = Followup().apply {
                name = event.fields[FIELD_NAME]
                email = event.fields[FIELD_EMAIL]
                languageCode = event.locale
                destination = event.fields[FIELD_DESTINATION]?.toLongOrNull()
            }

            // only store this followup if it's valid
            if (followup.isValid) {
                followupsRepository.createFollowup(followup)
                @Suppress("DeferredResultUnused")
                syncService.syncFollowupsAsync()
            }
        }
    }

    init {
        eventBus.register(this)
        coroutineScope.launch {
            @Suppress("DeferredResultUnused")
            this@FollowupService.syncService.syncFollowupsAsync()
        }
    }
}
