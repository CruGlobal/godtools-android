package org.cru.godtools.feature.bundledcontent.dagger

import dagger.Component
import javax.inject.Singleton
import org.ccci.gto.android.common.dagger.eager.EagerModule
import org.ccci.gto.android.common.dagger.eager.EagerSingletonInitializer
import org.cru.godtools.dagger.features.BundledContentFeatureDependencies
import org.cru.godtools.init.content.InitialContentModule

@Singleton
@Component(dependencies = [BundledContentFeatureDependencies::class], modules = [EagerModule::class, InitialContentModule::class])
interface BundledContentFeatureComponent {
    fun eagerSingletonInitializer(): EagerSingletonInitializer
}
