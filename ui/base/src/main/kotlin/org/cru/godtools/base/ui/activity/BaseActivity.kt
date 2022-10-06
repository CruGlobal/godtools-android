package org.cru.godtools.base.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.onDestroy
import org.ccci.gto.android.common.androidx.lifecycle.onResume
import org.ccci.gto.android.common.compat.content.getParcelableExtraCompat
import org.cru.godtools.base.Settings
import org.greenrobot.eventbus.EventBus

private const val EXTRA_FEATURE_DISCOVERY = "org.cru.godtools.BaseActivity.FEATURE_DISCOVERY"
private const val EXTRA_FEATURE = "org.cru.godtools.BaseActivity.FEATURE"
private const val EXTRA_FORCE = "org.cru.godtools.BaseActivity.FORCE"
private const val EXTRA_LAUNCHING_COMPONENT = "org.cru.godtools.BaseActivity.launchingComponent"

@VisibleForTesting
internal const val MSG_FEATURE_DISCOVERY = 1

abstract class BaseActivity protected constructor() : AppCompatActivity() {
    @Inject
    protected lateinit var eventBus: EventBus
    @Inject
    protected lateinit var settings: Settings

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFeatureDiscovery(savedInstanceState)
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)

        // update the Launching Component extra
        intent.putExtra(
            EXTRA_LAUNCHING_COMPONENT,
            newIntent.getParcelableExtraCompat(EXTRA_LAUNCHING_COMPONENT, Parcelable::class.java)
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.saveFeatureDiscoveryState()
    }
    // endregion Lifecycle

    // region Feature Discovery
    protected var featureDiscoveryActive: String? = null

    private fun setupFeatureDiscovery(savedInstanceState: Bundle?) {
        savedInstanceState?.restoreFeatureDiscoveryState()
        lifecycle.onResume { triggerFeatureDiscovery() }
    }

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
        Handler(mainLooper) { m -> showFeatureDiscovery(m) }
            .apply { lifecycle.onDestroy { removeCallbacksAndMessages(null) } }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun dispatchDelayedFeatureDiscovery(feature: String, force: Boolean, delay: Long) {
        val message = featureDiscoveryHandler.obtainMessage(MSG_FEATURE_DISCOVERY, feature).apply {
            data = Bundle().apply {
                putString(EXTRA_FEATURE, feature)
                putBoolean(EXTRA_FORCE, force)
            }
        }
        featureDiscoveryHandler.sendMessageDelayed(message, delay)
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
        if (upIntent.component == intent.getParcelableExtraCompat(EXTRA_LAUNCHING_COMPONENT, Parcelable::class.java)) {
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
        fun buildExtras(context: Context) = Bundle().apply {
            if (context is Activity) putParcelable(EXTRA_LAUNCHING_COMPONENT, context.componentName)
        }
    }
}
