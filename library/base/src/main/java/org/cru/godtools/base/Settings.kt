package org.cru.godtools.base

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import com.okta.oidc.clients.sessions.SessionClient
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import org.ccci.gto.android.common.androidx.lifecycle.getBooleanLiveData
import org.ccci.gto.android.common.androidx.lifecycle.getIntLiveData
import org.ccci.gto.android.common.androidx.lifecycle.getStringLiveData
import org.ccci.gto.android.common.okta.oidc.clients.sessions.oktaUserId

private const val PREFS_SETTINGS = "GodTools"
private const val PREF_ADDED_TO_CAMPAIGN = "added_to_campaign."
private const val PREF_LAUNCHES = "launches"
private const val PREF_VERSION_FIRST_LAUNCH = "version.firstLaunch"
private const val PREF_VERSION_LAST_LAUNCH = "version.lastLaunch"

private const val VERSION_5_1_4 = 4033503
private const val VERSION_5_2_0 = 4035089

@Singleton
class Settings @Inject internal constructor(
    @ApplicationContext private val context: Context,
    private val oktaSessionClient: Lazy<SessionClient>
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)

    companion object {
        const val PREF_PRIMARY_LANGUAGE = "languagePrimary"
        const val PREF_PARALLEL_LANGUAGE = "languageParallel"
        const val PREF_FEATURE_DISCOVERED = "feature_discovered."
        const val PREF_FEATURE_DISCOVERED_COUNT = "feature_discovered_count."

        // feature discovery
        const val FEATURE_LANGUAGE_SETTINGS = "languageSettings"
        const val FEATURE_LOGIN = "login"
        const val FEATURE_TOOL_OPENED = "toolOpened"
        const val FEATURE_TOOL_SHARE = "toolShare"
        const val FEATURE_TOOL_FAVORITE = "toolFavorite"
        const val FEATURE_TRACT_CARD_SWIPED = "tractCardSwiped"
        const val FEATURE_TRACT_CARD_CLICKED = "tractCardClicked"
        const val FEATURE_TUTORIAL_ONBOARDING = "tutorialOnboarding"
        const val FEATURE_TUTORIAL_TRAINING = "tutorialTraining"
        const val FEATURE_TUTORIAL_LIVE_SHARE = "tutorialLiveShare."
        const val FEATURE_TUTORIAL_TIPS = "tutorialTips."

        @JvmStatic
        val defaultLanguage: Locale get() = Locale.ENGLISH
    }

    // region Language Settings
    var primaryLanguage: Locale
        get() = prefs.getString(PREF_PRIMARY_LANGUAGE, null)?.parseLanguageTag()
            ?: defaultLanguage.also { primaryLanguage = it }
        set(value) {
            prefs.edit {
                putString(PREF_PRIMARY_LANGUAGE, value.toLanguageTag())
                if (value == parallelLanguage) remove(PREF_PARALLEL_LANGUAGE)
            }
        }
    val primaryLanguageLiveData by lazy {
        prefs.getStringLiveData(PREF_PRIMARY_LANGUAGE, defaultLanguage.toLanguageTag()).distinctUntilChanged()
            .map { it?.parseLanguageTag() ?: defaultLanguage.also { primaryLanguage = it } }
    }

    var parallelLanguage
        get() = prefs.getString(PREF_PARALLEL_LANGUAGE, null)?.parseLanguageTag()
        set(locale) {
            if (primaryLanguage == locale) return
            prefs.edit { putString(PREF_PARALLEL_LANGUAGE, locale?.toLanguageTag()) }
        }
    val parallelLanguageLiveData by lazy {
        prefs.getStringLiveData(PREF_PARALLEL_LANGUAGE, null).distinctUntilChanged()
            .map { it?.parseLanguageTag() }
    }

    fun isLanguageProtected(locale: Locale) = when (locale) {
        defaultLanguage -> true
        primaryLanguage -> true
        else -> false
    }
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
                FEATURE_TUTORIAL_ONBOARDING -> if (firstLaunchVersion <= VERSION_5_1_4) {
                    setFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING)
                    changed = true
                }
                FEATURE_LANGUAGE_SETTINGS -> if (parallelLanguage != null) {
                    setFeatureDiscovered(FEATURE_LANGUAGE_SETTINGS)
                    changed = true
                }
                FEATURE_LOGIN -> if (oktaSessionClient.get().oktaUserId != null) {
                    setFeatureDiscovered(FEATURE_LOGIN)
                    changed = true
                }
                FEATURE_TOOL_SHARE -> if (firstLaunchVersion <= VERSION_5_2_0) {
                    setFeatureDiscovered(FEATURE_TOOL_SHARE)
                    changed = true
                }
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
        get() = prefs.getInt(PREF_VERSION_FIRST_LAUNCH, BuildConfig.VERSION_CODE)
        set(value) = prefs.edit { putInt(PREF_VERSION_FIRST_LAUNCH, value) }
    private var lastLaunchVersion
        get() = prefs.getInt(PREF_VERSION_LAST_LAUNCH, -1).takeUnless { it == -1 }
        private set(value) = prefs.edit { putInt(PREF_VERSION_LAST_LAUNCH, value ?: BuildConfig.VERSION_CODE) }
    var launches
        get() = prefs.getInt(PREF_LAUNCHES, 0)
        private set(value) = prefs.edit { putInt(PREF_LAUNCHES, value) }

    private fun trackFirstLaunchVersion() {
        if (prefs.contains(PREF_VERSION_FIRST_LAUNCH)) return

        // The app was used before we started tracking the initial version, so just assume it was the most recent
        // version before we started tracking the first launch version
        if (prefs.contains(PREF_PRIMARY_LANGUAGE)) {
            firstLaunchVersion = VERSION_5_1_4
            return
        }

        // resolve the current version code as the first launch code
        firstLaunchVersion = BuildConfig.VERSION_CODE
    }

    fun trackLaunch() {
        lastLaunchVersion = BuildConfig.VERSION_CODE
        launches++
    }

    init {
        trackFirstLaunchVersion()
    }
    // endregion Launch tracking

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun String.parseLanguageTag() = Locale.forLanguageTag(this)
