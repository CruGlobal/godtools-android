package org.cru.godtools.article

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.cru.godtools.article.aem.AemArticleRendererModule
import org.cru.godtools.article.fragment.ArticlesFragment
import org.cru.godtools.article.fragment.CategoriesFragment

@Module(includes = [AemArticleRendererModule::class])
abstract class ArticleRendererModule {
    @ContributesAndroidInjector
    internal abstract fun articlesFragmentInjector(): ArticlesFragment

    @ContributesAndroidInjector
    internal abstract fun categoriesFragmentInjector(): CategoriesFragment
}
