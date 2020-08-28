package org.cru.godtools.article

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey
import org.cru.godtools.article.ui.articles.ArticlesFragmentDataModel

@Module
@InstallIn(SingletonComponent::class)
abstract class ArticleRendererModule {
    @Binds
    @IntoMap
    @ViewModelKey(ArticlesFragmentDataModel::class)
    internal abstract fun articlesFragmentDataModel(dataModel: ArticlesFragmentDataModel): ViewModel
}
