package org.cru.godtools.shortcuts;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.graphics.drawable.IconCompat;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Predicate;
import com.google.common.base.Strings;
import com.squareup.picasso.Picasso;

import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.util.LocaleUtils;
import org.ccci.gto.android.common.util.ThreadUtils;
import org.cru.godtools.base.Settings;
import org.cru.godtools.base.ui.util.ModelUtils;
import org.cru.godtools.base.util.FileUtils;
import org.cru.godtools.model.Attachment;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.event.AttachmentUpdateEvent;
import org.cru.godtools.model.event.ToolUpdateEvent;
import org.cru.godtools.model.event.TranslationUpdateEvent;
import org.cru.godtools.tract.activity.TractActivity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.GodToolsDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static org.cru.godtools.base.Settings.PREF_PARALLEL_LANGUAGE;
import static org.cru.godtools.base.Settings.PREF_PRIMARY_LANGUAGE;

public final class GodToolsShortcutManager implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final boolean SUPPORTS_SHORTCUT_MANAGER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
    private static final int MSG_UPDATE_SHORTCUTS = 1;
    private static final long DELAY_UPDATE_SHORTCUTS = 5000;

    private static final String TYPE_TOOL = "tool|";

    @NonNull
    private final Context mContext;
    @NonNull
    private final Settings mSettings;
    @NonNull
    private final GodToolsDao mDao;
    @NonNull
    private final Handler mHandler;

    private GodToolsShortcutManager(@NonNull final Context context) {
        mContext = context;
        mSettings = Settings.getInstance(context);
        mDao = GodToolsDao.getInstance(context);
        mHandler = new Handler(Looper.getMainLooper());

        // native ShortcutManager support
        if (SUPPORTS_SHORTCUT_MANAGER) {
            // register any appropriate event listeners
            EventBus.getDefault().register(this);
            mSettings.registerOnSharedPreferenceChangeListener(this);

            // enqueue an initial update
            enqueueUpdateShortcuts(true);
        }
    }

    @Nullable
    @SuppressLint("StaticFieldLeak")
    private static GodToolsShortcutManager sInstance;
    @NonNull
    @MainThread
    public static GodToolsShortcutManager getInstance(@NonNull final Context context) {
        synchronized (GodToolsShortcutManager.class) {
            if (sInstance == null) {
                sInstance = new GodToolsShortcutManager(context.getApplicationContext());
            }
        }

        return sInstance;
    }

    /* BEGIN lifecycle */

    /**
     * Called when the device locale was updated.
     */
    @MainThread
    public void onUpdateSystemLocale(@NonNull final BroadcastReceiver.PendingResult result) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            updateShortcuts();
            result.finish();
        });
    }

    /**
     * Called when the in-app language preferences are changed.
     */
    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        switch (Strings.nullToEmpty(key)) {
            case PREF_PRIMARY_LANGUAGE:
            case PREF_PARALLEL_LANGUAGE:
                enqueueUpdateShortcuts(false);
        }
    }

    /**
     * Handles potential icon image changes.
     */
    @AnyThread
    @Subscribe
    public void onAttachmentUpdate(@NonNull final AttachmentUpdateEvent event) {
        enqueueUpdateShortcuts(false);
    }

    @AnyThread
    @Subscribe
    public void onToolUpdate(@NonNull final ToolUpdateEvent event) {
        // Could change which tools are visible or the label for tools
        enqueueUpdateShortcuts(false);
    }

    @AnyThread
    @Subscribe
    public void onTranslationUpdate(@NonNull final TranslationUpdateEvent event) {
        // Could change which tools are available or the label for tools
        enqueueUpdateShortcuts(false);
    }

    /* END lifecycle */

    @AnyThread
    private void enqueueUpdateShortcuts(final boolean immediate) {
        // short-circuit if there isn't native ShortcutManager support
        if (!SUPPORTS_SHORTCUT_MANAGER) {
            return;
        }

        // cancel any pending update
        mHandler.removeMessages(MSG_UPDATE_SHORTCUTS);

        final Runnable task = () -> {
            if (ThreadUtils.isUiThread()) {
                AsyncTask.THREAD_POOL_EXECUTOR.execute(this::updateShortcuts);
            } else {
                updateShortcuts();
            }
        };

        // enqueue processing
        final Message m = Message.obtain(mHandler, task);
        m.what = MSG_UPDATE_SHORTCUTS;
        mHandler.sendMessageDelayed(m, immediate ? 0 : DELAY_UPDATE_SHORTCUTS);
    }

    @WorkerThread
    @TargetApi(Build.VERSION_CODES.N_MR1)
    synchronized void updateShortcuts() {
        final Map<String, ShortcutInfo> shortcuts = createAllShortcuts();
        updateDynamicShortcuts(shortcuts);
        updatePinnedShortcuts(shortcuts);
    }

    @WorkerThread
    @TargetApi(Build.VERSION_CODES.N_MR1)
    private void updateDynamicShortcuts(@NonNull final Map<String, ShortcutInfo> shortcuts) {
        final ShortcutManager manager = mContext.getSystemService(ShortcutManager.class);

        final List<ShortcutInfo> dynamic = mDao.streamCompat(
                Query.select(Tool.class)
                        .where(ToolTable.FIELD_ADDED.eq(true)))
                .map(GodToolsShortcutManager::toolShortcutId)
                .map(shortcuts::get)
                .filter(Predicate.Util.notNull())
                .limit(manager.getMaxShortcutCountPerActivity())
                .toList();
        manager.setDynamicShortcuts(dynamic);
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private void updatePinnedShortcuts(@NonNull final Map<String, ShortcutInfo> shortcuts) {
        final ShortcutManager manager = mContext.getSystemService(ShortcutManager.class);

        final List<String> invalid = Stream.of(manager.getPinnedShortcuts())
                .map(ShortcutInfo::getId)
                .filterNot(shortcuts::containsKey)
                .toList();
        manager.disableShortcuts(invalid);
        manager.enableShortcuts(new ArrayList<>(shortcuts.keySet()));
        manager.updateShortcuts(new ArrayList<>(shortcuts.values()));
    }

    @WorkerThread
    @TargetApi(Build.VERSION_CODES.N_MR1)
    private Map<String, ShortcutInfo> createAllShortcuts() {
        // create tool shortcuts
        return mDao.streamCompat(Query.select(Tool.class))
                .map(this::createToolShortcut)
                .flatMap(Optional::stream)
                .map(ShortcutInfoCompat::toShortcutInfo)
                .collect(Collectors.toMap(ShortcutInfo::getId));
    }

    @NonNull
    @WorkerThread
    private Optional<ShortcutInfoCompat> createToolShortcut(@NonNull final Tool tool) {
        // short-circuit if we don't have a valid tool code
        final String code = tool.getCode();
        if (code == null) {
            return Optional.empty();
        }

        // short-circuit if we don't have a primary translation
        final Translation translation = mDao.getLatestTranslation(code, mSettings.getPrimaryLanguage())
                .or(() -> mDao.getLatestTranslation(code, Locale.ENGLISH))
                .orElse(null);
        if (translation == null) {
            return Optional.empty();
        }

        // generate the list of locales to use for this tool
        final List<Locale> locales = new ArrayList<>();
        locales.add(translation.getLanguageCode());
        if (mSettings.getParallelLanguage() != null) {
            locales.add(mSettings.getParallelLanguage());
        }

        // generate the target intent for this shortcut
        final Intent intent;
        switch (tool.getType()) {
            case TRACT:
                intent = TractActivity.createIntent(mContext, code, locales.toArray(new Locale[0]));
                break;
            default:
                // XXX: we don't support shortcuts for this tool type
                return Optional.empty();
        }

        // Generate the shortcut label
        final Translation deviceTranslation = Stream.of(LocaleUtils.getFallbacks(Locale.getDefault(), Locale.ENGLISH))
                .map(locale -> mDao.getLatestTranslation(code, locale))
                .flatMap(Optional::stream)
                .findFirst().orElse(null);
        final CharSequence label = ModelUtils.getTranslationName(mContext, deviceTranslation, tool);

        // create the icon bitmap
        final Attachment banner = mDao.find(Attachment.class, tool.getDetailsBannerId());
        IconCompat icon = null;
        if (banner != null) {
            try {
                icon = IconCompat.createWithBitmap(
                        Picasso.with(mContext)
                                .load(FileUtils.getFile(mContext, banner.getLocalFileName()))
                                .transform(new CropCircleTransformation())
                                .get());
            } catch (final IOException ignored) {
            }
        }
        if (icon == null) {
            icon = IconCompat.createWithResource(mContext, R.mipmap.ic_launcher);
        }

        // build the shortcut
        return Optional.of(new ShortcutInfoCompat.Builder(mContext, toolShortcutId(tool))
                                   .setIntent(intent)
                                   .setShortLabel(label)
                                   .setLongLabel(label)
                                   .setIcon(icon)
                                   .build());
    }

    @NonNull
    private static String toolShortcutId(@NonNull final Tool tool) {
        return toolShortcutId(tool.getCode());
    }

    @NonNull
    private static String toolShortcutId(@Nullable final String tool) {
        return TYPE_TOOL + tool;
    }
}
