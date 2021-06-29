package org.cru.godtools.base.tool

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Named
import org.ccci.gto.android.common.androidx.lifecycle.net.isConnectedLiveData
import org.cru.godtools.base.ToolFileManager
import org.cru.godtools.tool.service.ManifestParser
import org.cru.godtools.tool.xml.AndroidXmlPullParserFactory
import org.greenrobot.eventbus.meta.SubscriberInfoIndex

@Module
@InstallIn(SingletonComponent::class)
object BaseToolRendererModule {
    const val IS_CONNECTED_LIVE_DATA = "LIVE_DATA_IS_CONNECTED"

    @Provides
    @Reusable
    @Named(IS_CONNECTED_LIVE_DATA)
    fun isConnectedLiveData(@ApplicationContext context: Context) = context.isConnectedLiveData()

    @Provides
    @Reusable
    fun manifestParser(fileManager: ToolFileManager) = ManifestParser(object : AndroidXmlPullParserFactory() {
        override suspend fun openFile(fileName: String) = fileManager.getInputStream(fileName).buffered()
    })

    @IntoSet
    @Provides
    @Reusable
    internal fun baseToolEventBusIndex(): SubscriberInfoIndex = BaseToolEventBusIndex()
}
