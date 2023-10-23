package org.cru.godtools.base

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import org.ccci.gto.android.common.androidx.core.content.localizeIfPossible

val Context.appLanguage: Locale
    get() = localizeIfPossible(AppCompatDelegate.getApplicationLocales())
        .getString(R.string.normalized_app_language)
        .let { Locale.forLanguageTag(it) }

fun Context.getAppLanguageFlow(): Flow<Locale> = flow {
    // TODO: is there a way to actively listen for changes?
    while (true) {
        emit(appLanguage)
        delay(1_000 / 60)
    }
}.distinctUntilChanged()
