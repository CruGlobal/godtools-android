package org.cru.godtools.base.tool

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
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
import org.cru.godtools.base.CONFIG_TOOL_CONTENT_FEATURE_PAGE_COLLECTION
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.base.tool.service.ContentEventAnalyticsHandler
import org.cru.godtools.shared.tool.parser.ManifestParser
import org.cru.godtools.shared.tool.parser.ParserConfig
import org.cru.godtools.shared.tool.parser.ParserConfig.Companion.FEATURE_ANIMATION
import org.cru.godtools.shared.tool.parser.ParserConfig.Companion.FEATURE_CONTENT_CARD
import org.cru.godtools.shared.tool.parser.ParserConfig.Companion.FEATURE_FLOW
import org.cru.godtools.shared.tool.parser.ParserConfig.Companion.FEATURE_MULTISELECT
import org.cru.godtools.shared.tool.parser.ParserConfig.Companion.FEATURE_PAGE_COLLECTION
import org.cru.godtools.shared.tool.parser.model.DeviceType
import org.cru.godtools.shared.tool.parser.xml.AndroidXmlPullParserFactory
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
        fun parserConfig(@ApplicationContext context: Context, remoteConfig: FirebaseRemoteConfig) = ParserConfig()
            .withAppVersion(DeviceType.ANDROID, context.versionName?.substringBefore("-"))
            .withSupportedFeatures(
                buildSet {
                    add(FEATURE_ANIMATION)
                    add(FEATURE_CONTENT_CARD)
                    add(FEATURE_FLOW)
                    add(FEATURE_MULTISELECT)
                    if (remoteConfig.getBoolean(CONFIG_TOOL_CONTENT_FEATURE_PAGE_COLLECTION)) {
                        add(FEATURE_PAGE_COLLECTION)
                    }
                }
            )

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
