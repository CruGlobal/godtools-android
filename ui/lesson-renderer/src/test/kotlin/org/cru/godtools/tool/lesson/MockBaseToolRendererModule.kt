package org.cru.godtools.tool.lesson

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Named
import okio.FileSystem
import okio.fakefilesystem.FakeFileSystem
import org.cru.godtools.base.tool.BaseToolRendererModule
import org.cru.godtools.shared.renderer.tips.InMemoryTipsRepository
import org.cru.godtools.shared.renderer.tips.TipsRepository

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [BaseToolRendererModule::class]
)
object MockBaseToolRendererModule {
    @get:[Provides Reusable Named(BaseToolRendererModule.IS_CONNECTED_LIVE_DATA)]
    val isConnectedLiveData: LiveData<Boolean> = MutableLiveData(true)

    @get:[Provides Reusable]
    val rendererTipsRepository: TipsRepository = InMemoryTipsRepository()

    @get:[Provides Reusable Named(BaseToolRendererModule.TOOL_RESOURCE_FILE_SYSTEM)]
    val resourceFileSystem: FileSystem = FakeFileSystem()
}
