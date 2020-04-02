package org.cru.godtools.base.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import org.ccci.gto.android.common.base.Constants.INVALID_LAYOUT_RES
import org.ccci.gto.android.common.dagger.viewmodel.DaggerSavedStateViewModelProviderFactory
import org.cru.godtools.base.ui.R2
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

private const val EXTRA_LAUNCHING_COMPONENT = "org.cru.godtools.BaseActivity.launchingComponent"

abstract class BaseActivity(@LayoutRes contentLayoutId: Int = INVALID_LAYOUT_RES) : AppCompatActivity(contentLayoutId) {
    @Inject
    protected lateinit var eventBus: EventBus

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    @CallSuper
    override fun onContentChanged() {
        super.onContentChanged()

        // HACK: manually trigger this ButterKnife view binding to work around an inheritance across libraries bug
        // HACK: see: https://github.com/JakeWharton/butterknife/issues/808
        BaseActivity_ViewBinding(this)

        ButterKnife.bind(this)
        setupActionBar()
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)

        // update the Launching Component extra
        intent.putExtra(EXTRA_LAUNCHING_COMPONENT, newIntent.getParcelableExtra<Parcelable>(EXTRA_LAUNCHING_COMPONENT))
    }

    @CallSuper
    protected open fun onSetupActionBar() = Unit
    // endregion Lifecycle

    // region ViewModelProvider.Factory
    @Inject
    internal lateinit var viewModelProviderFactory: DaggerSavedStateViewModelProviderFactory
    private val defaultViewModelProvider by lazy { viewModelProviderFactory.create(this, intent.extras) }

    override fun getDefaultViewModelProviderFactory() = defaultViewModelProvider
    // endregion ViewModelProvider.Factory

    // region ActionBar
    @JvmField
    @BindView(R2.id.appbar)
    var toolbar: Toolbar? = null

    @JvmField
    protected var actionBar: ActionBar? = null

    private fun setupActionBar() {
        toolbar?.let { setSupportActionBar(it) }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // trigger lifecycle event for subclasses
        actionBar = supportActionBar
        onSetupActionBar()
    }
    // endregion ActionBar

    // region Up Navigation
    override fun supportNavigateUpTo(upIntent: Intent) {
        // if the upIntent already points to the original launching activity, just finish this activity
        if (upIntent.component == intent.getParcelableExtra(EXTRA_LAUNCHING_COMPONENT)) {
            finish()
            return
        }

        // otherwise defer to default navigate behavior
        super.supportNavigateUpTo(upIntent)
    }

    override fun getSupportParentActivityIntent() = super.getSupportParentActivityIntent()?.apply {
        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        putExtras(buildParentIntentExtras())
    }

    @CallSuper
    protected open fun buildParentIntentExtras() = Bundle()
    // endregion Up Navigation

    companion object {
        @JvmStatic
        fun buildExtras(context: Context) = Bundle().apply {
            if (context is Activity) putParcelable(EXTRA_LAUNCHING_COMPONENT, context.componentName)
        }
    }
}
