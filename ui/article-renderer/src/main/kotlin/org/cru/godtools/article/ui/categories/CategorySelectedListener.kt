package org.cru.godtools.article.ui.categories

import org.cru.godtools.tool.model.Category

interface CategorySelectedListener {
    fun onCategorySelected(category: Category?)
}
