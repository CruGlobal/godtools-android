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
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.facebook.flipper.plugins.databases.DefaultSqliteDatabaseProvider
import org.ccci.gto.android.common.facebook.flipper.plugins.databases.SQLiteOpenHelperDatabaseConnectionProvider
import org.ccci.gto.android.common.okhttp3.util.addGlobalNetworkInterceptor
import org.keynote.godtools.android.db.GodToolsDatabase
import javax.inject.Singleton

@Module
abstract class FlipperModule {
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
                val networkPlugin = NetworkFlipperPlugin()
                addPlugin(networkPlugin)
                addGlobalNetworkInterceptor(FlipperOkhttpInterceptor(networkPlugin))
                start()
            }
        }

        @IntoSet
        @Provides
        @Singleton
        internal fun flipperDatabasePlugin(context: Context, db: GodToolsDatabase): FlipperPlugin =
            DatabasesFlipperPlugin(
                SqliteDatabaseDriver(
                    context,
                    DefaultSqliteDatabaseProvider(context),
                    SQLiteOpenHelperDatabaseConnectionProvider(context, dbs = *arrayOf(db))
                )
            )

        @Provides
        @ElementsIntoSet
        @EagerSingleton(EagerSingleton.ThreadMode.MAIN_ASYNC)
        internal fun flipperClientEagerSingleton(flipperClient: FlipperClient?) =
            listOfNotNull<Any>(flipperClient).toSet()
    }
}
