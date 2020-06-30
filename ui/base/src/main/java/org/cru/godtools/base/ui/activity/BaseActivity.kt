package org.cru.godtools.base.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import butterknife.BindView
import butterknife.ButterKnife
import dagger.android.AndroidInjection
import org.ccci.gto.android.common.androidx.lifecycle.onDestroy
import org.ccci.gto.android.common.base.Constants.INVALID_LAYOUT_RES
import org.ccci.gto.android.common.dagger.viewmodel.DaggerSavedStateViewModelProviderFactory
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.R2
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

private const val EXTRA_FEATURE_DISCOVERY = "org.cru.godtools.BaseActivity.FEATURE_DISCOVERY"
private const val EXTRA_FEATURE = "org.cru.godtools.BaseActivity.FEATURE"
private const val EXTRA_FORCE = "org.cru.godtools.BaseActivity.FORCE"
private const val EXTRA_LAUNCHING_COMPONENT = "org.cru.godtools.BaseActivity.launchingComponent"

@VisibleForTesting
internal const val MSG_FEATURE_DISCOVERY = 1

abstract class BaseActivity(@LayoutRes contentLayoutId: Int = INVALID_LAYOUT_RES) : AppCompatActivity(contentLayoutId) {
    @Inject
    protected lateinit var eventBus: EventBus
    @Inject
    protected lateinit var settings: Settings

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        savedInstanceState?.restoreFeatureDiscoveryState()
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

    override fun onPostResume() {
        super.onPostResume()
        triggerFeatureDiscovery()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.saveFeatureDiscoveryState()
    }
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
    @Deprecated("Use supportActionBar instead", ReplaceWith("supportActionBar"))
    protected var actionBar: ActionBar? = null

    private fun setupActionBar() {
        toolbar?.let { setSupportActionBar(it) }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // trigger lifecycle event for subclasses
        actionBar = supportActionBar
        onSetupActionBar()
    }
    // endregion ActionBar

    // region Feature Discovery
    protected var featureDiscoveryActive: String? = null

    private fun triggerFeatureDiscovery() {
        when (val feature = featureDiscoveryActive) {
            null -> showNextFeatureDiscovery()
            else -> showFeatureDiscovery(feature, true)
        }
    }

    protected open fun showNextFeatureDiscovery() = Unit

    protected fun showFeatureDiscovery(feature: String, force: Boolean = false) {
        // short-circuit if this activity is not started
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return

        // short-circuit if feature discovery is already visible
        if (isFeatureDiscoveryVisible()) return

        // short-circuit if this feature was discovered and we aren't forcing it
        if (settings.isFeatureDiscovered(feature) && !force) return

        // short-circuit if we can't show this feature discovery right now,
        // and try to show the next feature discovery that can be shown.
        if (!canShowFeatureDiscovery(feature)) {
            showNextFeatureDiscovery()
            return
        }

        // actually show the feature
        onShowFeatureDiscovery(feature, force)
    }

    @CallSuper
    protected open fun onShowFeatureDiscovery(feature: String, force: Boolean) = Unit

    /**
     * @return true if the activity is in a state that it can actually show the specified feature discovery.
     */
    @CallSuper
    protected open fun canShowFeatureDiscovery(feature: String) = true

    @CallSuper
    protected open fun isFeatureDiscoveryVisible() = false

    private fun Bundle.saveFeatureDiscoveryState() {
        putString(EXTRA_FEATURE_DISCOVERY, featureDiscoveryActive)
    }

    private fun Bundle.restoreFeatureDiscoveryState() {
        featureDiscoveryActive = getString(EXTRA_FEATURE_DISCOVERY, featureDiscoveryActive)
    }

    // region Delayed Dispatch
    @VisibleForTesting
    internal val featureDiscoveryHandler by lazy {
        Handler(mainLooper, Handler.Callback { m -> showFeatureDiscovery(m) })
            .apply { lifecycle.onDestroy { removeCallbacksAndMessages(null) } }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun dispatchDelayedFeatureDiscovery(feature: String, force: Boolean, delay: Long) {
        featureDiscoveryHandler.sendMessageDelayed(
            featureDiscoveryHandler.obtainMessage(MSG_FEATURE_DISCOVERY, feature).apply {
                data = Bundle().apply {
                    putString(EXTRA_FEATURE, feature)
                    putBoolean(EXTRA_FORCE, force)
                }
            }, delay
        )
    }

    private fun showFeatureDiscovery(message: Message): Boolean {
        message.data.getString(EXTRA_FEATURE)?.let {
            showFeatureDiscovery(it, message.data.getBoolean(EXTRA_FORCE, false))
        }
        return true
    }

    protected fun purgeQueuedFeatureDiscovery(feature: String) {
        featureDiscoveryHandler.removeMessages(MSG_FEATURE_DISCOVERY, feature)
    }
    // endregion Delayed Dispatch
    // endregion Feature Discovery

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
