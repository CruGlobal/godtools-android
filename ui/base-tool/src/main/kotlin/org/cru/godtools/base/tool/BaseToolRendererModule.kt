package org.cru.godtools.base.tool

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Named
import org.ccci.gto.android.common.androidx.lifecycle.net.isConnectedLiveData
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.base.tool.service.ContentEventAnalyticsHandler
import org.cru.godtools.tool.FEATURE_ANIMATION
import org.cru.godtools.tool.FEATURE_CONTENT_CARD
import org.cru.godtools.tool.FEATURE_FLOW
import org.cru.godtools.tool.FEATURE_MULTISELECT
import org.cru.godtools.tool.ParserConfig
import org.cru.godtools.tool.model.DeviceType
import org.cru.godtools.tool.service.ManifestParser
import org.cru.godtools.tool.xml.AndroidXmlPullParserFactory
import org.greenrobot.eventbus.meta.SubscriberInfoIndex

@Module
@InstallIn(SingletonComponent::class)
abstract class BaseToolRendererModule {
    @Binds
    @IntoSet
    @EagerSingleton(on = EagerSingleton.LifecycleEvent.ACTIVITY_CREATED)
    abstract fun contentEventAnalyticsHandler(handler: ContentEventAnalyticsHandler): Any

    companion object {
        const val IS_CONNECTED_LIVE_DATA = "LIVE_DATA_IS_CONNECTED"

        @Provides
        @Reusable
        @Named(IS_CONNECTED_LIVE_DATA)
        fun isConnectedLiveData(@ApplicationContext context: Context) = context.isConnectedLiveData()

        @Provides
        @Reusable
        fun parserConfig(@ApplicationContext context: Context) = ParserConfig()
            .withAppVersion(DeviceType.ANDROID, context.versionName.substringBefore("-"))
            .withSupportedFeatures(setOf(FEATURE_ANIMATION, FEATURE_CONTENT_CARD, FEATURE_FLOW, FEATURE_MULTISELECT))

        @Provides
        @Reusable
        fun manifestParser(fs: ToolFileSystem, config: ParserConfig) = ManifestParser(
            object : AndroidXmlPullParserFactory() {
                override suspend fun openFile(fileName: String) = fs.openInputStream(fileName).buffered()
            },
            config
        )

        @IntoSet
        @Provides
        @Reusable
        internal fun baseToolEventBusIndex(): SubscriberInfoIndex = BaseToolEventBusIndex()
    }
}

private val Context.versionName
    get() = packageManager.getPackageInfo(packageName, 0).versionName
