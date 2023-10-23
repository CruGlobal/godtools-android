package org.cru.godtools.base

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.distinctUntilChanged
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import org.ccci.gto.android.common.androidx.lifecycle.getBooleanLiveData
import org.ccci.gto.android.common.androidx.lifecycle.getIntLiveData
import org.ccci.gto.android.common.kotlin.coroutines.getBooleanFlow

private const val PREFS_SETTINGS = "GodTools"
private const val PREF_ADDED_TO_CAMPAIGN = "added_to_campaign."
private const val PREF_LAUNCHES = "launches"
private const val PREF_VERSION_FIRST_LAUNCH = "version.firstLaunch"
private const val PREF_VERSION_LAST_LAUNCH = "version.lastLaunch"

@Singleton
class Settings internal constructor(private val context: Context, coroutineScope: CoroutineScope) {
    @Inject
    internal constructor(@ApplicationContext context: Context) : this(context, CoroutineScope(Dispatchers.Default))

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)

    companion object {
        const val PREF_FEATURE_DISCOVERED = "feature_discovered."
        const val PREF_FEATURE_DISCOVERED_COUNT = "feature_discovered_count."

        // feature discovery
        const val FEATURE_LANGUAGE_SETTINGS = "languageSettings"
        const val FEATURE_TOOL_OPENED = "toolOpened"
        const val FEATURE_TOOL_SHARE = "toolShare"
        const val FEATURE_TOOL_FAVORITE = "toolFavorite"
        const val FEATURE_LESSON_FEEDBACK = "lessonFeedback."
        const val FEATURE_TRACT_CARD_SWIPED = "tractCardSwiped"
        const val FEATURE_TRACT_CARD_CLICKED = "tractCardClicked"
        const val FEATURE_TUTORIAL_ONBOARDING = "tutorialOnboarding"
        const val FEATURE_TUTORIAL_FEATURES = "tutorialTraining"
        const val FEATURE_TUTORIAL_LIVE_SHARE = "tutorialLiveShare."
        const val FEATURE_TUTORIAL_TIPS = "tutorialTips."

        @JvmStatic
        val defaultLanguage: Locale get() = Locale.ENGLISH
    }

    // region Language Settings
    var appLanguage: Locale
        get() = context.appLanguage
        set(value) = AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(value))

    val appLanguageFlow: Flow<Locale> = context.getAppLanguageFlow()
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(5_000))
        .onStart { emit(context.appLanguage) }
        .distinctUntilChanged()
    // endregion Language Settings

    // region Feature Discovery Tracking
    fun setFeatureDiscovered(feature: String) = prefs.edit {
        putBoolean("$PREF_FEATURE_DISCOVERED$feature", true)
        putInt("$PREF_FEATURE_DISCOVERED_COUNT$feature", prefs.getInt("$PREF_FEATURE_DISCOVERED_COUNT$feature", 0) + 1)
    }

    fun isFeatureDiscovered(feature: String): Boolean {
        val discovered = isFeatureDiscoveredInt(feature)

        // handle pre-conditions that would indicate a feature was already discovered
        if (!discovered) {
            var changed = false
            when (feature) {
                // HACK: placeholder to keep this logic for now
                FEATURE_LANGUAGE_SETTINGS -> Unit
            }
            if (changed) return isFeatureDiscoveredInt(feature)
        }

        return discovered
    }

    fun getFeatureDiscoveredCount(feature: String): Int {
        // perform a simple lookup to initialize the feature when necessary
        isFeatureDiscovered(feature)
        return prefs.getInt("$PREF_FEATURE_DISCOVERED_COUNT$feature", 0)
    }

    fun isFeatureDiscoveredFlow(feature: String) = prefs.getBooleanFlow("$PREF_FEATURE_DISCOVERED$feature", false)
        // perform a simple lookup to initialize the feature when necessary
        .onStart { emit(isFeatureDiscovered(feature)) }
        .distinctUntilChanged()

    fun isFeatureDiscoveredLiveData(feature: String): LiveData<Boolean> {
        // perform a simple lookup to initialize the feature when necessary
        isFeatureDiscovered(feature)
        return prefs.getBooleanLiveData("$PREF_FEATURE_DISCOVERED$feature", false).distinctUntilChanged()
    }

    fun getFeatureDiscoveredCountLiveData(feature: String): LiveData<Int> {
        // perform a simple lookup to initialize the feature when necessary
        isFeatureDiscovered(feature)
        return prefs.getIntLiveData("$PREF_FEATURE_DISCOVERED_COUNT$feature", 0).distinctUntilChanged()
    }

    private fun isFeatureDiscoveredInt(feature: String) = prefs.getBoolean("$PREF_FEATURE_DISCOVERED$feature", false)
    // endregion Feature Discovery Tracking

    // region Campaign Tracking
    fun isAddedToCampaign(oktaId: String? = null, guid: String? = null) = when {
        oktaId == null && guid == null -> true
        oktaId?.let { prefs.getBoolean("$PREF_ADDED_TO_CAMPAIGN$oktaId", false) } == true -> true
        guid?.let { prefs.getBoolean("$PREF_ADDED_TO_CAMPAIGN${guid.uppercase(Locale.ROOT)}", false) } == true -> true
        else -> false
    }

    fun recordAddedToCampaign(oktaId: String? = null, guid: String? = null) = prefs.edit {
        if (oktaId != null) putBoolean("$PREF_ADDED_TO_CAMPAIGN$oktaId", true)
        if (guid != null) putBoolean("$PREF_ADDED_TO_CAMPAIGN${guid.uppercase(Locale.ROOT)}", true)
    }
    // endregion Campaign Tracking

    // region Launch tracking
    @VisibleForTesting
    internal var firstLaunchVersion
        get() = prefs.getLong(PREF_VERSION_FIRST_LAUNCH, context.versionCode)
        set(value) = prefs.edit { putLong(PREF_VERSION_FIRST_LAUNCH, value) }
    private var lastLaunchVersion
        get() = prefs.getLong(PREF_VERSION_LAST_LAUNCH, -1).takeUnless { it == -1L }
        private set(value) = prefs.edit { putLong(PREF_VERSION_LAST_LAUNCH, value ?: context.versionCode) }
    var launches
        get() = prefs.getInt(PREF_LAUNCHES, 0)
        private set(value) = prefs.edit { putInt(PREF_LAUNCHES, value) }

    private fun trackFirstLaunchVersion() {
        if (prefs.contains(PREF_VERSION_FIRST_LAUNCH)) return
        firstLaunchVersion = context.versionCode
    }

    fun trackLaunch() {
        lastLaunchVersion = context.versionCode
        launches++
    }

    init {
        trackFirstLaunchVersion()
    }
    // endregion Launch tracking
}

private val Context.versionCode
    get() = PackageInfoCompat.getLongVersionCode(packageManager.getPackageInfo(packageName, 0))
