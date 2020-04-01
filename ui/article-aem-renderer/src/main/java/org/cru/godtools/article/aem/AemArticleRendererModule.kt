package org.cru.godtools.article.aem

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.cru.godtools.article.aem.fragment.AemArticleFragment

@Module
abstract class AemArticleRendererModule {
    @ContributesAndroidInjector
    internal abstract fun aemArticleFragmentInjector(): AemArticleFragment
}
