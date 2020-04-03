package org.cru.godtools.article.aem

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey
import org.cru.godtools.article.aem.activity.AemArticleActivity
import org.cru.godtools.article.aem.fragment.AemArticleFragment
import org.cru.godtools.article.aem.fragment.AemArticleViewModel

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
}
