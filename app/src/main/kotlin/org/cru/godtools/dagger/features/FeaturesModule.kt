package org.cru.godtools.dagger.features

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import javax.inject.Singleton
import org.ccci.gto.android.common.dagger.FirstNonNullCachingProvider
import org.ccci.gto.android.common.dagger.splitinstall.SplitInstallComponent
import org.ccci.gto.android.common.util.lang.getClassOrNull
import org.ccci.gto.android.common.util.lang.getDeclaredMethodOrNull

@Module
@InstallIn(SingletonComponent::class)
object FeaturesModule {
    @IntoMap
    @Provides
    @Singleton
    @StringKey("bundledcontent")
    fun provideBundledContentComponentProvider(
        @ApplicationContext context: Context
    ): FirstNonNullCachingProvider<SplitInstallComponent> {
        val dependencies = EntryPointAccessors.fromApplication<BundledContentFeatureDependencies>(context)
        return FirstNonNullCachingProvider {
            getClassOrNull("org.cru.godtools.feature.bundledcontent.dagger.BundledContentFeatureComponentKt")
                ?.getDeclaredMethodOrNull("create", BundledContentFeatureDependencies::class.java)
                ?.invoke(null, dependencies) as? SplitInstallComponent
        }
    }
}
