package org.cru.godtools.tract.service

import android.os.AsyncTask
import androidx.annotation.WorkerThread
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import org.ccci.gto.android.common.dagger.getValue
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.db.repository.FollowupsRepository
import org.cru.godtools.model.Followup
import org.cru.godtools.shared.tool.parser.model.EventId
import org.cru.godtools.sync.GodToolsSyncService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

private const val FIELD_NAME = "name"
private const val FIELD_EMAIL = "email"
private const val FIELD_DESTINATION = "destination_id"

@Singleton
class FollowupService @Inject internal constructor(
    eventBus: EventBus,
    followupsRepository: Lazy<FollowupsRepository>,
    private val syncService: Lazy<GodToolsSyncService>
) {
    init {
        eventBus.register(this)

        // sync any currently pending followups
        AsyncTask.THREAD_POOL_EXECUTOR.execute { syncPendingFollowups() }
    }

    private val followupsRepository by followupsRepository

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onContentEvent(event: Event) {
        if (event.id == EventId.FOLLOWUP) {
            val followup = Followup().apply {
                name = event.fields[FIELD_NAME]
                email = event.fields[FIELD_EMAIL]
                languageCode = event.locale
                destination = event.fields[FIELD_DESTINATION]?.toLongOrNull()
            }

            // only store this followup if it's valid
            if (followup.isValid) {
                runBlocking {
                    followupsRepository.createFollowup(followup)
                }

                syncPendingFollowups()
            }
        }
    }

    @WorkerThread
    private fun syncPendingFollowups() = syncService.get().syncFollowups().sync()
}
