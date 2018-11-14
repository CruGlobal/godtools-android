package org.cru.godtools.article.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.fragment.app.transaction
import org.cru.godtools.article.R
import org.cru.godtools.article.fragment.CategoriesFragment
import org.cru.godtools.article.fragment.newCategoriesFragment
import org.cru.godtools.base.tool.activity.BaseArticleActivity
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import org.cru.godtools.xml.model.Category
import java.util.Locale

private const val TAG_MAIN_FRAGMENT = "mainFragment"

fun Context.createCategoriesIntent(toolCode: String, language: Locale): Intent {
    return Intent(this, CategoriesActivity::class.java)
        .putExtras(BaseSingleToolActivity.buildExtras(this, toolCode, language))
}

fun Activity.startCategoriesActivity(toolCode: String, language: Locale) {
    startActivity(createCategoriesIntent(toolCode, language))
}

class CategoriesActivity : BaseArticleActivity(false), CategoriesFragment.Callbacks {
    // region Lifecycle Events

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        setContentView(R.layout.activity_generic_tool_fragment)
    }

    override fun onStart() {
        super.onStart()
        loadInitialFragmentIfNeeded()
    }

    override fun onCategorySelected(category: Category?) = startArticlesActivity(tool, locale, category?.id)

    // endregion Lifecycle Events

    @MainThread
    private fun loadInitialFragmentIfNeeded() {
        supportFragmentManager?.apply {
            if (findFragmentByTag(TAG_MAIN_FRAGMENT) == null) {
                transaction {
                    replace(R.id.frame, newCategoriesFragment(tool, locale), TAG_MAIN_FRAGMENT)
                }
            }
        }
    }
}
