package org.cru.godtools.dagger

import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.core.FlipperClient
import com.facebook.flipper.core.FlipperPlugin
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.databases.impl.SqliteDatabaseDriver
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoSet
import javax.inject.Singleton
import okhttp3.Interceptor
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.dagger.okhttp3.InterceptorType
import org.ccci.gto.android.common.dagger.okhttp3.InterceptorType.Type.NETWORK_INTERCEPTOR
import org.ccci.gto.android.common.facebook.flipper.plugins.databases.DefaultSqliteDatabaseProvider
import org.ccci.gto.android.common.facebook.flipper.plugins.databases.SQLiteOpenHelperDatabaseConnectionProvider
import org.keynote.godtools.android.db.GodToolsDatabase

@Module
@InstallIn(SingletonComponent::class)
abstract class FlipperModule {
    @Binds
    @IntoSet
    abstract fun flipperPlugins(networkFlipperPlugin: NetworkFlipperPlugin): FlipperPlugin

    companion object {
        @Provides
        @Singleton
        internal fun provideFlipper(
            context: Context,
            plugins: Lazy<Set<@JvmSuppressWildcards FlipperPlugin>>
        ): FlipperClient? {
            if (!FlipperUtils.shouldEnableFlipper(context)) return null

            SoLoader.init(context, false)
            return AndroidFlipperClient.getInstance(context).apply {
                addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
                addPlugin(SharedPreferencesFlipperPlugin(context))
                plugins.get().forEach { addPlugin(it) }
                start()
            }
        }

        @IntoSet
        @Provides
        @Singleton
        internal fun databasesFlipperPlugin(context: Context, db: GodToolsDatabase): FlipperPlugin =
            DatabasesFlipperPlugin(
                SqliteDatabaseDriver(
                    context,
                    DefaultSqliteDatabaseProvider(context),
                    SQLiteOpenHelperDatabaseConnectionProvider(context, dbs = *arrayOf(db))
                )
            )

        @Provides
        @Singleton
        internal fun networkFlipperPlugin() = NetworkFlipperPlugin()

        @IntoSet
        @Provides
        @Singleton
        @InterceptorType(NETWORK_INTERCEPTOR)
        fun flipperOkHttpInterceptor(networkFlipperPlugin: NetworkFlipperPlugin): Interceptor =
            FlipperOkhttpInterceptor(networkFlipperPlugin)

        @Provides
        @ElementsIntoSet
        @EagerSingleton(threadMode = EagerSingleton.ThreadMode.ASYNC)
        internal fun flipperClientEagerSingleton(flipperClient: FlipperClient?) =
            listOfNotNull<Any>(flipperClient).toSet()
    }
}
