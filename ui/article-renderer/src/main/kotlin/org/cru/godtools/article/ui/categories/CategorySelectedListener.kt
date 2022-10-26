package org.cru.godtools.article.ui.categories

import org.cru.godtools.shared.tool.parser.model.Category

interface CategorySelectedListener {
    fun onCategorySelected(category: Category?)
}
