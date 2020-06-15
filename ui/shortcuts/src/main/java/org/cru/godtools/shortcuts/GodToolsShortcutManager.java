package org.cru.godtools.shortcuts;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.base.Strings;

import org.cru.godtools.base.Settings;
import org.cru.godtools.model.event.AttachmentUpdateEvent;
import org.cru.godtools.model.event.ToolUpdateEvent;
import org.cru.godtools.model.event.TranslationUpdateEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.keynote.godtools.android.db.GodToolsDao;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;

import static org.cru.godtools.base.Settings.PREF_PARALLEL_LANGUAGE;
import static org.cru.godtools.base.Settings.PREF_PRIMARY_LANGUAGE;

@Singleton
public class GodToolsShortcutManager extends KotlinGodToolsShortcutManager
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject
    GodToolsShortcutManager(@NonNull final Context context, @NonNull final GodToolsDao dao,
                            @NonNull final EventBus eventBus, @NonNull final Settings settings) {
        super(context, dao, settings);

        // register any appropriate event listeners
        eventBus.register(this);
        settings.registerOnSharedPreferenceChangeListener(this);
    }

    // region Lifecycle Events

    /**
     * Called when the in-app language preferences are changed.
     */
    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        switch (Strings.nullToEmpty(key)) {
            case PREF_PRIMARY_LANGUAGE:
            case PREF_PARALLEL_LANGUAGE:
                launchUpdateShortcutsJob(false);
                launchUpdatePendingShortcutsJob(false);
        }
    }

    /**
     * Handles potential icon image changes.
     */
    @AnyThread
    @Subscribe
    public void onAttachmentUpdate(@NonNull final AttachmentUpdateEvent event) {
        launchUpdateShortcutsJob(false);
        launchUpdatePendingShortcutsJob(false);
    }

    @AnyThread
    @Subscribe
    public void onToolUpdate(@NonNull final ToolUpdateEvent event) {
        // Could change which tools are visible or the label for tools
        launchUpdateShortcutsJob(false);
        launchUpdatePendingShortcutsJob(false);
    }

    @AnyThread
    @Subscribe
    public void onTranslationUpdate(@NonNull final TranslationUpdateEvent event) {
        // Could change which tools are available or the label for tools
        launchUpdateShortcutsJob(false);
        launchUpdatePendingShortcutsJob(false);
    }
    // endregion Lifecycle Events
}
