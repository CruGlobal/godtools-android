package org.cru.godtools.article

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey
import org.cru.godtools.article.aem.AemArticleRendererModule
import org.cru.godtools.article.ui.articles.ArticlesActivity
import org.cru.godtools.article.ui.articles.ArticlesFragment
import org.cru.godtools.article.ui.articles.ArticlesFragmentDataModel
import org.cru.godtools.article.ui.categories.CategoriesActivity
import org.cru.godtools.article.ui.categories.CategoriesFragment

@Module(includes = [AemArticleRendererModule::class])
@InstallIn(SingletonComponent::class)
abstract class ArticleRendererModule {
    @ContributesAndroidInjector
    internal abstract fun articlesActivityInjector(): ArticlesActivity

    @ContributesAndroidInjector
    internal abstract fun articlesFragmentInjector(): ArticlesFragment

    @Binds
    @IntoMap
    @ViewModelKey(ArticlesFragmentDataModel::class)
    internal abstract fun articlesFragmentDataModel(dataModel: ArticlesFragmentDataModel): ViewModel

    @ContributesAndroidInjector
    internal abstract fun categoriesActivityInjector(): CategoriesActivity

    @ContributesAndroidInjector
    internal abstract fun categoriesFragmentInjector(): CategoriesFragment
}
