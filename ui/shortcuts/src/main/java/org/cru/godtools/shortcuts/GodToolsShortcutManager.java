package org.cru.godtools.shortcuts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.common.base.Strings;

import org.ccci.gto.android.common.util.ThreadUtils;
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
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import static org.cru.godtools.base.Settings.PREF_PARALLEL_LANGUAGE;
import static org.cru.godtools.base.Settings.PREF_PRIMARY_LANGUAGE;

@Singleton
public class GodToolsShortcutManager extends KotlinGodToolsShortcutManager
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final boolean SUPPORTS_SHORTCUT_MANAGER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
    private static final int MSG_UPDATE_PENDING_SHORTCUTS = 2;
    private static final long DELAY_UPDATE_PENDING_SHORTCUTS = 100;

    @NonNull
    private final Handler mHandler;

    @Inject
    GodToolsShortcutManager(@NonNull final Context context, @NonNull final GodToolsDao dao,
                            @NonNull final Settings settings) {
        super(context, dao, settings);
        mHandler = new Handler(Looper.getMainLooper());

        // native ShortcutManager support
        if (SUPPORTS_SHORTCUT_MANAGER) {
            // register any appropriate event listeners
            EventBus.getDefault().register(this);
            settings.registerOnSharedPreferenceChangeListener(this);

            // enqueue an initial update
            launchUpdateShortcutsJob(true);
        }
    }

    // region Lifecycle Events

    /**
     * Called when the device locale was updated.
     */
    @MainThread
    public void onUpdateSystemLocale(@NonNull final BroadcastReceiver.PendingResult result) {
        if (SUPPORTS_SHORTCUT_MANAGER) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
                updateShortcuts();
                updatePendingShortcuts();
                result.finish();
            });
        } else {
            result.finish();
        }
    }

    /**
     * Called when the in-app language preferences are changed.
     */
    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        switch (Strings.nullToEmpty(key)) {
            case PREF_PRIMARY_LANGUAGE:
            case PREF_PARALLEL_LANGUAGE:
                launchUpdateShortcutsJob(false);
                enqueueUpdatePendingShortcuts(false);
        }
    }

    /**
     * Handles potential icon image changes.
     */
    @AnyThread
    @Subscribe
    public void onAttachmentUpdate(@NonNull final AttachmentUpdateEvent event) {
        launchUpdateShortcutsJob(false);
        enqueueUpdatePendingShortcuts(false);
    }

    @AnyThread
    @Subscribe
    public void onToolUpdate(@NonNull final ToolUpdateEvent event) {
        // Could change which tools are visible or the label for tools
        launchUpdateShortcutsJob(false);
        enqueueUpdatePendingShortcuts(false);
    }

    @AnyThread
    @Subscribe
    public void onTranslationUpdate(@NonNull final TranslationUpdateEvent event) {
        // Could change which tools are available or the label for tools
        launchUpdateShortcutsJob(false);
        enqueueUpdatePendingShortcuts(false);
    }
    // endregion Lifecycle Events

    // region Pending shortcut
    @AnyThread
    private void enqueueUpdatePendingShortcuts(final boolean immediate) {
        // cancel any pending update
        mHandler.removeMessages(MSG_UPDATE_PENDING_SHORTCUTS);

        final Runnable task = () -> {
            if (ThreadUtils.isUiThread()) {
                AsyncTask.THREAD_POOL_EXECUTOR.execute(this::updatePendingShortcuts);
            } else {
                updatePendingShortcuts();
            }
        };

        // enqueue processing
        final Message m = Message.obtain(mHandler, task);
        m.what = MSG_UPDATE_PENDING_SHORTCUTS;
        mHandler.sendMessageDelayed(m, immediate ? 0 : DELAY_UPDATE_PENDING_SHORTCUTS);
    }
    // endregion Pending shortcut
}
