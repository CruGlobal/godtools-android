package org.cru.godtools.article.aem

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.db.enableMigrations
import org.cru.godtools.article.aem.fragment.AemArticleFragment
import org.cru.godtools.article.aem.service.AemArticleManager
import org.cru.godtools.article.aem.ui.AemArticleActivity
import org.cru.godtools.article.aem.ui.AemArticleViewModel
import javax.inject.Singleton

@Module
abstract class AemArticleRendererModule {
    @ContributesAndroidInjector
    internal abstract fun aemArticleActivityInjector(): AemArticleActivity

    @ContributesAndroidInjector
    internal abstract fun aemArticleFragmentInjector(): AemArticleFragment

    @Binds
    @IntoMap
    @ViewModelKey(AemArticleViewModel::class)
    internal abstract fun aemArticleViewModel(dataModel: AemArticleViewModel): ViewModel

    @Binds
    @IntoSet
    @EagerSingleton(on = EagerSingleton.LifecycleEvent.ACTIVITY_CREATED, threadMode = EagerSingleton.ThreadMode.ASYNC)
    internal abstract fun aemArticleMangerEagerSingleton(aemArticleManger: AemArticleManager): Any

    companion object {
        @Provides
        @Singleton
        fun articleRoomDatabase(context: Context) =
            Room.databaseBuilder(context, ArticleRoomDatabase::class.java, ArticleRoomDatabase.DATABASE_NAME)
                .enableMigrations()
                .build()

        @Reusable
        @Provides
        fun articleDao(db: ArticleRoomDatabase) = db.articleDao()

        @Reusable
        @Provides
        fun resourceDao(db: ArticleRoomDatabase) = db.resourceDao()
    }
}
