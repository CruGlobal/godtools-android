package org.cru.godtools.sync

import android.content.Context
import dagger.Module
import dagger.Provides

@Module
abstract class SyncModule {
    companion object {
        // TODO: make this a Dagger singleton
        @Provides
        fun godToolsSyncService(context: Context): GodToolsSyncService = GodToolsSyncService.getInstance(context)
    }
}
