package org.cru.godtools.article.aem

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import org.ccci.gto.android.common.dagger.eager.EagerSingleton
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey
import org.cru.godtools.article.aem.activity.AemArticleActivity
import org.cru.godtools.article.aem.activity.AemArticleActivityDataModel
import org.cru.godtools.article.aem.fragment.AemArticleFragment
import org.cru.godtools.article.aem.fragment.AemArticleViewModel
import org.cru.godtools.article.aem.service.AemArticleManager

@Module
abstract class AemArticleRendererModule {
    @ContributesAndroidInjector
    internal abstract fun aemArticleActivityInjector(): AemArticleActivity

    @Binds
    @IntoMap
    @ViewModelKey(AemArticleActivityDataModel::class)
    internal abstract fun aemArticleActivityDataModel(dataModel: AemArticleActivityDataModel): ViewModel

    @ContributesAndroidInjector
    internal abstract fun aemArticleFragmentInjector(): AemArticleFragment

    @Binds
    @IntoMap
    @ViewModelKey(AemArticleViewModel::class)
    internal abstract fun aemArticleViewModel(dataModel: AemArticleViewModel): ViewModel

    @Binds
    @IntoSet
    @EagerSingleton(threadMode = EagerSingleton.ThreadMode.BACKGROUND)
    internal abstract fun aemArticleMangerEagerSingleton(aemArticleManger: AemArticleManager): Any
}
