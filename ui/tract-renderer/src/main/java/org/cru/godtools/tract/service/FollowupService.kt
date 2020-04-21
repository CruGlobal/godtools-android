package org.cru.godtools.tract.service

import android.content.Context
import android.os.AsyncTask
import androidx.annotation.WorkerThread
import dagger.Lazy
import org.cru.godtools.base.model.Event
import org.cru.godtools.model.Followup
import org.cru.godtools.sync.syncFollowups
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.keynote.godtools.android.db.GodToolsDao
import javax.inject.Inject
import javax.inject.Singleton

private const val FIELD_NAME = "name"
private const val FIELD_EMAIL = "email"
private const val FIELD_DESTINATION = "destination_id"

@Singleton
class FollowupService @Inject internal constructor(
    private val context: Context,
    eventBus: EventBus,
    private val dao: Lazy<GodToolsDao>
) {
    init {
        eventBus.register(this)

        // sync any currently pending followups
        AsyncTask.THREAD_POOL_EXECUTOR.execute { syncPendingFollowups() }
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onContentEvent(event: Event) {
        if (event.id == Event.Id.FOLLOWUP_EVENT) {
            val followup = Followup().apply {
                name = event.fields[FIELD_NAME]
                email = event.fields[FIELD_EMAIL]
                languageCode = event.locale
                destination = event.fields[FIELD_DESTINATION]?.toLongOrNull()
            }

            // only store this followup if it's valid
            if (followup.isValid) {
                dao.get().insertNew(followup)
                syncPendingFollowups()
            }
        }
    }

    @WorkerThread
    private fun syncPendingFollowups() = context.syncFollowups().sync()
}
