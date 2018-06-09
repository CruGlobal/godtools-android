package org.cru.godtools.shortcuts;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.os.AsyncTask;
import android.os.Build;
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
import com.squareup.picasso.Picasso;

import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.util.LocaleUtils;
import org.cru.godtools.R;
import org.cru.godtools.base.Settings;
import org.cru.godtools.base.util.FileUtils;
import org.cru.godtools.model.Attachment;
import org.cru.godtools.model.Translation;
import org.cru.godtools.tract.activity.TractActivity;
import org.cru.godtools.util.ModelUtils;
import org.keynote.godtools.android.activity.MainActivity;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.model.Tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public final class GodToolsShortcutManager {
    private static final String TYPE_TOOL = "tool|";

    @NonNull
    private final Context mContext;
    @NonNull
    private final Settings mSettings;
    @NonNull
    private final GodToolsDao mDao;

    private GodToolsShortcutManager(@NonNull final Context context) {
        mContext = context;
        mSettings = Settings.getInstance(context);
        mDao = GodToolsDao.getInstance(context);

        // perform an initial update
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(this::updateShortcuts);
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

    @WorkerThread
    @TargetApi(Build.VERSION_CODES.N_MR1)
    void updateShortcuts() {
        final Map<String, ShortcutInfo> shortcuts = createAllShortcuts();
        updateDynamicShortcuts(shortcuts);
    }

    @WorkerThread
    @TargetApi(Build.VERSION_CODES.N_MR1)
    void updateDynamicShortcuts(@NonNull final Map<String, ShortcutInfo> shortcuts) {
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
                                   .setActivity(new ComponentName(mContext, MainActivity.class))
                                   .setIntent(intent)
                                   .setShortLabel(label)
                                   .setLongLabel(label)
                                   .setIcon(icon)
                                   .build());
    }

    @NonNull
    private static String toolShortcutId(@NonNull final Tool tool) {
        return TYPE_TOOL + tool.getCode();
    }
}
