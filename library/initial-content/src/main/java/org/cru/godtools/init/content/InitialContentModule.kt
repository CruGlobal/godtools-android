package org.cru.godtools.init.content

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton

@Module
abstract class InitialContentModule {
    @Binds
    @IntoSet
    @EagerSingleton(on = EagerSingleton.LifecycleEvent.ACTIVITY_CREATED, threadMode = EagerSingleton.ThreadMode.ASYNC)
    abstract fun initialContentImporter(importer: InitialContentImporter): Any
}
