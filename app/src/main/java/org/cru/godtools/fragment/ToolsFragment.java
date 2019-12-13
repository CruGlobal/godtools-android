package org.cru.godtools.fragment;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.NinePatchDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Expression.Field;
import org.ccci.gto.android.common.db.Join;
import org.ccci.gto.android.common.db.Table;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.cru.godtools.R;
import org.cru.godtools.adapter.BaseHeaderFooterAdapter;
import org.cru.godtools.adapter.EmptyListHeaderFooterAdapter;
import org.cru.godtools.adapter.EmptyListHeaderFooterAdapter.Builder;
import org.cru.godtools.adapter.ToolsAdapter;
import org.cru.godtools.base.Settings;
import org.cru.godtools.content.ToolsCursorLoader;
import org.cru.godtools.databinding.FragmentToolsBinding;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.model.Language;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.event.ToolUpdateEvent;
import org.cru.godtools.model.event.content.AttachmentEventBusSubscriber;
import org.cru.godtools.sync.GodToolsSyncServiceKt;
import org.cru.godtools.tutorial.PageSet;
import org.cru.godtools.tutorial.activity.TutorialActivityKt;
import org.greenrobot.eventbus.EventBus;
import org.keynote.godtools.android.db.Contract.AttachmentTable;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.db.GodToolsDao;

import java.util.Locale;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

public class ToolsFragment extends BasePlatformFragment
        implements ToolsAdapter.Callbacks, BaseHeaderFooterAdapter.EmptyCallbacks {
    private static final String EXTRA_MODE = ToolsFragment.class.getName() + ".MODE";

    public interface Callbacks {
        void onToolInfo(@Nullable String code);

        void onToolSelect(@Nullable String code, @NonNull Tool.Type type, Locale... languages);

        void onNoToolsAvailableAction();
    }

    public interface TutorialCallbacks {
        void onTutorialClose();

        void onTutorialOpen();
    }

    private TutorialCallbacks mTutorialCallbacks = new TutorialCallbacks() {
        @Override
        public void onTutorialClose() {
            closeTutorial();
        }

        @Override
        public void onTutorialOpen() {
            openTutorial();
        }
    };

    public static final int MODE_ADDED = 1;
    public static final int MODE_AVAILABLE = 2;

    private static final int LOADER_TOOLS = 101;

    @Nullable
    private GodToolsDao mDao;

    private final CursorLoaderCallbacks mCursorLoaderCallbacks = new CursorLoaderCallbacks();

    // these properties should be treated as final and only set/modified in onCreate()
    /*final*/ int mMode = MODE_ADDED;

    FragmentToolsBinding mToolsBinding;

    @Nullable
    private RecyclerViewDragDropManager mToolsDragDropManager;
    @Nullable
    private RecyclerView.Adapter mToolsDragDropAdapter;
    @Nullable
    private EmptyListHeaderFooterAdapter mToolsHeaderAdapter;
    @Nullable
    private ToolsAdapter mToolsAdapter;

    @Nullable
    private Cursor mResources;

    public static Fragment newInstance(final int mode) {
        final Fragment fragment = new ToolsFragment();
        final Bundle args = new Bundle(1);
        args.putInt(EXTRA_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    // region Lifecycle
    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context != null) {
            mDao = GodToolsDao.getInstance(context);
        }
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mMode = args.getInt(EXTRA_MODE, mMode);
        }

        startLoaders();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @NonNull final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        mToolsBinding = FragmentToolsBinding.inflate(inflater, container, false);
        return mToolsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolsList();
        mToolsBinding.setCallback(mTutorialCallbacks);
    }

    @Override
    public void onResume() {
        super.onResume();
        onUpdateFeatureDiscovery();
    }

    void onLoadResources(@Nullable final Cursor cursor) {
        mResources = cursor;
        updateToolsList();
    }

    @Override
    protected void onUpdatePrimaryLanguage() {
        super.onUpdatePrimaryLanguage();
        restartToolsLoader();
    }

    @Override
    protected void onUpdateParallelLanguage() {
        super.onUpdateParallelLanguage();
        restartToolsLoader();
    }

    @Override
    protected void onUpdateFeatureDiscovery(@NonNull final String feature) {
        super.onUpdateFeatureDiscovery(feature);
        mToolsBinding.setIsTutorialViewable(!settings.isFeatureDiscovered(Settings.FEATURE_TUTORIAL_TRAINING));
    }

    @Override
    public void onToolSelect(@Nullable final String code, @NonNull final Tool.Type type, final Locale... languages) {
        final Callbacks listener = FragmentUtils.getListener(this, Callbacks.class);
        if (listener != null) {
            listener.onToolSelect(code, type, languages);
        }
    }

    @Override
    public void onToolInfo(@Nullable final String code) {
        final Callbacks listener = FragmentUtils.getListener(this, Callbacks.class);
        if (listener != null) {
            listener.onToolInfo(code);
        }
    }

    @Override
    public void onToolAdd(@Nullable final String code) {
        if (code != null) {
            GodToolsDownloadManager.getInstance(requireContext()).addTool(code);
        }
    }

    @Override
    public void onToolsReordered(final long... ids) {
        if (mDao != null) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
                mDao.updateToolOrder(ids);
                EventBus.getDefault().post(ToolUpdateEvent.INSTANCE);
            });
        }
    }

    @Override
    public void onEmptyActionClick() {
        final Callbacks listener = FragmentUtils.getListener(this, Callbacks.class);
        if (listener != null) {
            listener.onNoToolsAvailableAction();
        }
    }

    @Override
    public void onPause() {
        if (mToolsDragDropManager != null) {
            mToolsDragDropManager.cancelDrag();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        cleanupToolsList();
        super.onDestroyView();
    }
    // endregion Lifecycle

    private boolean showDownloading() {
        return mMode == MODE_AVAILABLE;
    }

    private void closeTutorial() {
        settings.setFeatureDiscovered(Settings.FEATURE_TUTORIAL_TRAINING);
    }

    private void openTutorial() {
        if (getActivity() != null) {
            TutorialActivityKt.startTutorialActivity(getActivity(), PageSet.TRAINING);
        }
    }

    @CallSuper
    protected void syncData(final boolean force) {
        super.syncData(force);
        getSyncHelper().sync(GodToolsSyncServiceKt.syncTools(requireContext(), force));
    }

    private void startLoaders() {
        restartToolsLoader();
    }

    private void restartToolsLoader() {
        getLoaderManager().restartLoader(LOADER_TOOLS, null, mCursorLoaderCallbacks);
    }

    @SuppressWarnings("unchecked")
    private void setupToolsList() {
        if (mToolsBinding.resources != null) {
            mToolsBinding.resources.setHasFixedSize(false);

            // create base tools adapter
            mToolsAdapter = new ToolsAdapter();
            mToolsAdapter.setCallbacks(this);
            RecyclerView.Adapter adapter = mToolsAdapter;

            // configure the DragDrop RecyclerView components (Only for Added tools)
            if (mMode == MODE_ADDED) {
                mToolsBinding.resources.setItemAnimator(new DraggableItemAnimator());
                mToolsDragDropManager = new RecyclerViewDragDropManager();
                mToolsDragDropManager.setDraggingItemShadowDrawable((NinePatchDrawable) ContextCompat
                        .getDrawable(requireActivity(), R.drawable.material_shadow_z3));
                mToolsDragDropManager.setInitiateOnLongPress(true);
                mToolsDragDropManager.setInitiateOnMove(false);
                mToolsDragDropAdapter = mToolsDragDropManager.createWrappedAdapter(adapter);
                adapter = mToolsDragDropAdapter;
            }

            // configure empty list view if required for the current mode
            switch (mMode) {
                case MODE_ADDED:
                    mToolsHeaderAdapter = new Builder()
                            .layout(R.layout.list_item_none_large_icon)
                            .emptyIcon(R.drawable.ic_find_tools)
                            .emptyAction(R.string.nav_find_tools)
                            .build();
                    break;
                case MODE_AVAILABLE:
                    mToolsHeaderAdapter = new Builder()
                            .emptyText(R.string.text_tools_all_installed)
                            .build();
                    break;
                default:
                    mToolsHeaderAdapter = null;
            }
            if (mToolsHeaderAdapter != null) {
                mToolsHeaderAdapter.setEmptyCallbacks(this);
                mToolsHeaderAdapter.setAdapter(adapter);
                adapter = mToolsHeaderAdapter;
            }

            // attach the correct adapter to the tools RecyclerView
            mToolsBinding.resources.setAdapter(adapter);

            // handle some post-adapter configuration
            if (mToolsDragDropManager != null) {
                mToolsDragDropManager.attachRecyclerView(mToolsBinding.resources);
            }

            updateToolsList();
        }
    }

    private void updateToolsList() {
        if (mToolsHeaderAdapter != null) {
            mToolsHeaderAdapter.setShowEmptyFooter(mResources != null && mResources.getCount() == 0);
        }
        if (mToolsAdapter != null) {
            mToolsAdapter.swapCursor(mResources);
        }
    }

    private void cleanupToolsList() {
        if (mToolsHeaderAdapter != null) {
            mToolsHeaderAdapter.setEmptyCallbacks(null);
        }
        if (mToolsAdapter != null) {
            mToolsAdapter.setCallbacks(null);
        }
        if (mToolsBinding.resources != null) {
            mToolsBinding.resources.setItemAnimator(null);
            mToolsBinding.resources.setAdapter(null);
        }
        if (mToolsDragDropManager != null) {
            mToolsDragDropManager.release();
        }
        WrapperAdapterUtils.releaseAll(mToolsDragDropAdapter);

        mToolsHeaderAdapter = null;
        mToolsDragDropAdapter = null;
        mToolsDragDropManager = null;
        mToolsAdapter = null;
        mToolsBinding = null;
    }

    class CursorLoaderCallbacks extends SimpleLoaderCallbacks<Cursor> {
        @Nullable
        @Override
        public Loader<Cursor> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_TOOLS:
                    return new LocalToolsCursorLoader(requireContext(), args, mMode, getPrimaryLanguage(),
                                                      getParallelLanguage());
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Cursor> loader, @Nullable final Cursor cursor) {
            switch (loader.getId()) {
                case LOADER_TOOLS:
                    onLoadResources(cursor);
                    break;
            }
        }
    }

    static class LocalToolsCursorLoader extends ToolsCursorLoader {
        // set repetitive TranslationTable objects for primary, parallel, and default
        private static final int TRANS_COUNT = 3;
        private static final int TRANS_PRIM = 0;
        private static final int TRANS_PARA = 1;
        private static final int TRANS_DEF = 2;
        private static final String[] TRANS_ALIAS = new String[TRANS_COUNT];
        private static final Table[] TRANS_TABLE = new Table[TRANS_COUNT];
        private static final Join[] TRANS_JOIN = new Join[TRANS_COUNT];
        private static final Field[] TRANS_FIELD_LANG = new Field[TRANS_COUNT];

        static {
            for (int i = 0; i < TRANS_COUNT; i++) {
                TRANS_ALIAS[i] = "trans" + i;
                TRANS_TABLE[i] = TranslationTable.TABLE.as(TRANS_ALIAS[i]);
                TRANS_FIELD_LANG[i] = TRANS_TABLE[i].field(TranslationTable.COLUMN_LANGUAGE);
                //noinspection unchecked
                TRANS_JOIN[i] = Join.create(TRANS_TABLE[i]).type("LEFT")
                        .on(TRANS_TABLE[i].field(TranslationTable.COLUMN_TOOL).eq(ToolTable.FIELD_CODE))
                        .andOn(TRANS_TABLE[i].field(TranslationTable.COLUMN_PUBLISHED).eq(true));
            }
        }

        private static final String[] TOOLS_PROJECTION = {
                ToolTable.COLUMN_ID,
                ToolTable.COLUMN_CODE,
                ToolTable.COLUMN_TYPE,
                // title
                "coalesce(" +
                        TRANS_ALIAS[TRANS_PRIM] + "." + TranslationTable.COLUMN_NAME + "," +
                        TRANS_ALIAS[TRANS_DEF] + "." + TranslationTable.COLUMN_NAME + "," +
                        ToolTable.TABLE_NAME + "." + ToolTable.COLUMN_NAME + ") " +
                        "AS " + ToolsAdapter.COL_TITLE,
                // title_lang
                "CASE " +
                        "WHEN " + TRANS_ALIAS[TRANS_PRIM] + "." + TranslationTable.COLUMN_NAME + " IS NOT NULL THEN " +
                        TRANS_ALIAS[TRANS_PRIM] + "." + TranslationTable.COLUMN_LANGUAGE + " " +
                        "WHEN " + TRANS_ALIAS[TRANS_DEF] + "." + TranslationTable.COLUMN_NAME + " IS NOT NULL THEN " +
                        TRANS_ALIAS[TRANS_DEF] + "." + TranslationTable.COLUMN_LANGUAGE + " " +
                        "ELSE NULL END AS " + ToolsAdapter.COL_TITLE_LANGUAGE,
                // tagline
                "coalesce(" +
                        TRANS_ALIAS[TRANS_PRIM] + "." + TranslationTable.COLUMN_TAGLINE + "," +
                        TRANS_ALIAS[TRANS_PRIM] + "." + TranslationTable.COLUMN_DESCRIPTION + "," +
                        TRANS_ALIAS[TRANS_DEF] + "." + TranslationTable.COLUMN_TAGLINE + "," +
                        TRANS_ALIAS[TRANS_DEF] + "." + TranslationTable.COLUMN_DESCRIPTION + "," +
                        ToolTable.TABLE_NAME + "." + ToolTable.COLUMN_DESCRIPTION + ") " +
                        "AS " + ToolsAdapter.COL_TAGLINE,
                // tagline_lang
                "CASE " +
                        "WHEN " + TRANS_ALIAS[TRANS_PRIM] + "." + TranslationTable.COLUMN_TAGLINE +
                        " IS NOT NULL THEN " + TRANS_ALIAS[TRANS_PRIM] + "." + TranslationTable.COLUMN_LANGUAGE + " " +
                        "WHEN " + TRANS_ALIAS[TRANS_PRIM] + "." + TranslationTable.COLUMN_DESCRIPTION +
                        " IS NOT NULL THEN " + TRANS_ALIAS[TRANS_PRIM] + "." + TranslationTable.COLUMN_LANGUAGE + " " +
                        "WHEN " + TRANS_ALIAS[TRANS_DEF] + "." + TranslationTable.COLUMN_TAGLINE +
                        " IS NOT NULL THEN " + TRANS_ALIAS[TRANS_DEF] + "." + TranslationTable.COLUMN_LANGUAGE + " " +
                        "WHEN " + TRANS_ALIAS[TRANS_DEF] + "." + TranslationTable.COLUMN_DESCRIPTION +
                        " IS NOT NULL THEN " + TRANS_ALIAS[TRANS_DEF] + "." + TranslationTable.COLUMN_LANGUAGE + " " +
                        "ELSE NULL END AS " + ToolsAdapter.COL_TAGLINE_LANGUAGE,
                ToolTable.COLUMN_SHARES,
                ToolTable.COLUMN_PENDING_SHARES,
                ToolTable.COLUMN_ADDED,
                TRANS_ALIAS[TRANS_PRIM] + "." + TranslationTable.COLUMN_LANGUAGE + " AS " +
                        ToolsAdapter.COL_PRIMARY_LANGUAGE,
                TRANS_ALIAS[TRANS_PARA] + "." + TranslationTable.COLUMN_LANGUAGE + " AS " +
                        ToolsAdapter.COL_PARALLEL_LANGUAGE,
                TRANS_ALIAS[TRANS_DEF] + "." + TranslationTable.COLUMN_LANGUAGE + " AS " +
                        ToolsAdapter.COL_DEFAULT_LANGUAGE,
                AttachmentTable.TABLE_NAME + "." + AttachmentTable.COLUMN_LOCALFILENAME + " AS " +
                        ToolsAdapter.COL_BANNER
        };

        static final Join TOOLS_JOIN_BANNER =
                ToolTable.SQL_JOIN_BANNER.type("LEFT").andOn(AttachmentTable.SQL_WHERE_DOWNLOADED);

        private final int mMode;

        LocalToolsCursorLoader(@NonNull final Context context, @Nullable final Bundle args, final int mode,
                               @NonNull final Locale primaryLanguage, @Nullable final Locale parallelLanguage) {
            super(context, args);
            addEventBusSubscriber(new AttachmentEventBusSubscriber(this));
            setProjection(TOOLS_PROJECTION);
            //noinspection unchecked
            setJoins(
                    TOOLS_JOIN_BANNER,
                    TRANS_JOIN[TRANS_PRIM].andOn(TRANS_FIELD_LANG[TRANS_PRIM].eq(primaryLanguage)),
                    TRANS_JOIN[TRANS_PARA].andOn(TRANS_FIELD_LANG[TRANS_PARA]
                                                         .eq(parallelLanguage != null ? parallelLanguage :
                                                                     Language.INVALID_CODE)),
                    TRANS_JOIN[TRANS_DEF].andOn(TRANS_FIELD_LANG[TRANS_DEF].eq(Settings.getDefaultLanguage()))
            );
            setGroupBy(ToolTable.FIELD_CODE);

            mMode = mode;
        }

        @Nullable
        @Override
        public Expression getWhere() {
            final Expression where = ToolTable.FIELD_TYPE.ne(Tool.Type.UNKNOWN);
            switch (mMode) {
                case MODE_ADDED:
                case MODE_AVAILABLE:
                    return where.and(ToolTable.FIELD_ADDED.eq(mMode == MODE_ADDED));
                default:
                    return where;
            }
        }

        @Nullable
        @Override
        public String getSortOrder() {
            switch (mMode) {
                case MODE_ADDED:
                    return ToolTable.COLUMN_ORDER;
                case MODE_AVAILABLE:
                default:
                    return null;
            }
        }
    }
}
