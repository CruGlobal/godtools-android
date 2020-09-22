package org.cru.godtools.feature.bundledcontent.dagger

import androidx.annotation.Keep
import dagger.Component
import javax.inject.Singleton
import org.ccci.gto.android.common.dagger.eager.EagerModule
import org.ccci.gto.android.common.dagger.splitinstall.eager.SplitInstallEagerSingletonInitializerProvider
import org.cru.godtools.dagger.features.BundledContentFeatureDependencies
import org.cru.godtools.init.content.InitialContentModule

@Singleton
@Component(
    dependencies = [BundledContentFeatureDependencies::class],
    modules = [EagerModule::class, InitialContentModule::class]
)
interface BundledContentFeatureComponent : SplitInstallEagerSingletonInitializerProvider

@Keep
@JvmSynthetic
fun create(dependencies: BundledContentFeatureDependencies) =
    DaggerBundledContentFeatureComponent.builder().bundledContentFeatureDependencies(dependencies).build()
