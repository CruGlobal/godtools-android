package org.cru.godtools.tract

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey
import org.cru.godtools.tract.activity.ModalActivity
import org.cru.godtools.tract.activity.ModalActivityDataModel
import org.cru.godtools.tract.activity.TractActivity
import org.greenrobot.eventbus.meta.SubscriberInfoIndex

@Module
abstract class TractRendererModule {
    @ContributesAndroidInjector
    internal abstract fun tractActivityInjector(): TractActivity

    @ContributesAndroidInjector
    internal abstract fun modalActivityInjector(): ModalActivity

    @Binds
    @IntoMap
    @ViewModelKey(ModalActivityDataModel::class)
    internal abstract fun modalActivityDataModel(dataModel: ModalActivityDataModel): ViewModel

    companion object {
        @IntoSet
        @Provides
        @Reusable
        fun tractEventBusIndex(): SubscriberInfoIndex = TractEventBusIndex()
    }
}
