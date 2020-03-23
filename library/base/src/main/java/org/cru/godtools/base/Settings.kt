package org.cru.godtools.base

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import me.thekey.android.TheKey
import org.ccci.gto.android.common.androidx.lifecycle.getStringLiveData
import org.ccci.gto.android.common.compat.util.LocaleCompat.forLanguageTag
import org.ccci.gto.android.common.compat.util.LocaleCompat.toLanguageTag
import org.cru.godtools.base.util.SingletonHolder
import java.util.Locale

private const val PREFS_SETTINGS = "GodTools"
private const val PREF_ADDED_TO_CAMPAIGN = "added_to_campaign."
private const val PREF_LAUNCHES = "launches"
private const val PREF_VERSION_FIRST_LAUNCH = "version.firstLaunch"
private const val PREF_VERSION_LAST_LAUNCH = "version.lastLaunch"

private const val VERSION_5_1_4 = 4033503

class Settings private constructor(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)

    companion object : SingletonHolder<Settings, Context>({ Settings(it.applicationContext) }) {
        const val PREF_PRIMARY_LANGUAGE = "languagePrimary"
        const val PREF_PARALLEL_LANGUAGE = "languageParallel"
        const val PREF_FEATURE_DISCOVERED = "feature_discovered."

        // feature discovery
        const val FEATURE_LANGUAGE_SETTINGS = "languageSettings"
        const val FEATURE_LOGIN = "login"
        const val FEATURE_TOOL_OPENED = "toolOpened"
        const val FEATURE_TRACT_CARD_SWIPED = "tractCardSwiped"
        const val FEATURE_TRACT_CARD_CLICKED = "tractCardClicked"
        const val FEATURE_TUTORIAL_TRAINING = "tutorialTraining"
        const val FEATURE_TUTORIAL_ONBOARDING = "tutorialOnboarding"

        @JvmStatic
        val defaultLanguage: Locale get() = Locale.ENGLISH
    }

    // region Language Settings
    var primaryLanguage: Locale
        get() = prefs.getString(PREF_PRIMARY_LANGUAGE, null)?.parseLanguageTag()
            ?: defaultLanguage.also { primaryLanguage = it }
        set(value) {
            prefs.edit {
                putString(PREF_PRIMARY_LANGUAGE, toLanguageTag(value))
                if (value == parallelLanguage) remove(PREF_PARALLEL_LANGUAGE)
            }
        }
    val primaryLanguageLiveData by lazy {
        prefs.getStringLiveData(PREF_PRIMARY_LANGUAGE, toLanguageTag(defaultLanguage)).distinctUntilChanged()
            .map { it?.parseLanguageTag() ?: defaultLanguage.also { primaryLanguage = it } }
    }

    var parallelLanguage
        get() = prefs.getString(PREF_PARALLEL_LANGUAGE, null)?.parseLanguageTag()
        set(locale) {
            if (primaryLanguage == locale) return
            prefs.edit { putString(PREF_PARALLEL_LANGUAGE, locale?.let { toLanguageTag(it) }) }
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
    fun setFeatureDiscovered(feature: String) = prefs.edit { putBoolean("$PREF_FEATURE_DISCOVERED$feature", true) }

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
                FEATURE_LOGIN -> if (TheKey.getInstance(context).defaultSessionGuid != null) {
                    setFeatureDiscovered(FEATURE_LOGIN)
                    changed = true
                }
            }
            if (changed) return isFeatureDiscoveredInt(feature)
        }

        return discovered
    }

    private fun isFeatureDiscoveredInt(feature: String) = prefs.getBoolean("$PREF_FEATURE_DISCOVERED$feature", false)
    // endregion Feature Discovery Tracking

    // region Campaign Tracking
    fun isAddedToCampaign(guid: String) =
        prefs.getBoolean(PREF_ADDED_TO_CAMPAIGN + guid.toUpperCase(Locale.ROOT), false)

    fun setAddedToCampaign(guid: String, added: Boolean) =
        prefs.edit { putBoolean(PREF_ADDED_TO_CAMPAIGN + guid.toUpperCase(Locale.ROOT), added) }
    // endregion Campaign Tracking

    // region Launch tracking
    private var firstLaunchVersion
        get() = prefs.getInt(PREF_VERSION_FIRST_LAUNCH, BuildConfig.VERSION_CODE)
        set(value) = prefs.edit { putInt(PREF_VERSION_FIRST_LAUNCH, value) }
    var lastLaunchVersion
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

private inline fun String.parseLanguageTag() = forLanguageTag(this)
