package org.cru.godtools.tract

import androidx.lifecycle.ViewModel
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.viewmodel.AssistedSavedStateViewModelFactory
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey
import org.cru.godtools.tract.activity.ModalActivity
import org.cru.godtools.tract.activity.ModalActivityDataModel
import org.cru.godtools.tract.activity.TractActivity
import org.cru.godtools.tract.activity.TractActivityDataModel
import org.cru.godtools.tract.liveshare.TractPublisherController
import org.cru.godtools.tract.liveshare.TractSubscriberController
import org.greenrobot.eventbus.meta.SubscriberInfoIndex

@AssistedModule
@Module(includes = [AssistedInject_TractRendererModule::class])
abstract class TractRendererModule {
    @ContributesAndroidInjector
    internal abstract fun tractActivityInjector(): TractActivity

    @Binds
    @IntoMap
    @ViewModelKey(TractActivityDataModel::class)
    abstract fun tractActivityDataModel(f: TractActivityDataModel.Factory):
        AssistedSavedStateViewModelFactory<out ViewModel>

    @ContributesAndroidInjector
    internal abstract fun modalActivityInjector(): ModalActivity

    @Binds
    @IntoMap
    @ViewModelKey(TractPublisherController::class)
    abstract fun tractPublisherController(f: TractPublisherController.Factory):
        AssistedSavedStateViewModelFactory<out ViewModel>

    @Binds
    @IntoMap
    @ViewModelKey(TractSubscriberController::class)
    abstract fun tractSubscriberController(controller: TractSubscriberController): ViewModel

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
