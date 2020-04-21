package org.cru.godtools.article.ui.categories

import org.cru.godtools.xml.model.Category

interface CategorySelectedListener {
    fun onCategorySelected(category: Category?)
}
