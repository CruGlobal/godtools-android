package org.cru.godtools

import androidx.work.WorkManager
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.ccci.gto.android.common.dagger.eager.EagerModule
import org.cru.godtools.dagger.ServicesModule
import org.mockito.Mockito
import org.mockito.kotlin.mock

@Module(includes = [EagerModule::class])
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ServicesModule::class]
)
class ExternalSingletonsModule {
    @get:Provides
    val picasso by lazy { mock<Picasso>(defaultAnswer = Mockito.RETURNS_DEEP_STUBS) }
    @get:Provides
    val workManager by lazy { mock<WorkManager>() }
}
