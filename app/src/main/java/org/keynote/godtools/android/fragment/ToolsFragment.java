package org.keynote.godtools.android.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Expression.Field;
import org.ccci.gto.android.common.db.Join;
import org.ccci.gto.android.common.db.Table;
import org.ccci.gto.android.common.eventbus.content.DaoCursorEventBusLoader;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.cru.godtools.R;
import org.cru.godtools.adapter.BaseHeaderFooterAdapter;
import org.cru.godtools.adapter.EmptyListHeaderFooterAdapter;
import org.cru.godtools.adapter.EmptyListHeaderFooterAdapter.Builder;
import org.cru.godtools.adapter.ToolsAdapter;
import org.cru.godtools.base.Settings;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.model.Language;
import org.cru.godtools.model.event.content.AttachmentEventBusSubscriber;
import org.cru.godtools.sync.GodToolsSyncService;
import org.keynote.godtools.android.content.ToolsCursorLoader;
import org.keynote.godtools.android.db.Contract.AttachmentTable;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.model.Tool;

import java.util.Locale;

import butterknife.BindView;

public class ToolsFragment extends BaseFragment
        implements ToolsAdapter.Callbacks, BaseHeaderFooterAdapter.EmptyCallbacks {
    private static final String EXTRA_MODE = ToolsFragment.class.getName() + ".MODE";

    public interface Callbacks {
        void onToolInfo(@Nullable String code);

        void onToolSelect(@Nullable String code, @NonNull Tool.Type type, Locale... languages);

        void onNoToolsAvailableAction();
    }

    private static final int MODE_ADDED = 1;
    private static final int MODE_AVAILABLE = 2;

    private static final int LOADER_TOOLS = 101;

    private final CursorLoaderCallbacks mCursorLoaderCallbacks = new CursorLoaderCallbacks();

    // these properties should be treated as final and only set/modified in onCreate()
    /*final*/ int mMode = MODE_ADDED;

    @Nullable
    @BindView(R.id.resources)
    RecyclerView mResourcesView;
    @Nullable
    private EmptyListHeaderFooterAdapter mToolsHeaderAdapter;
    @Nullable
    private ToolsAdapter mToolsAdapter;

    @Nullable
    private Cursor mResources;

    public static Fragment newAddedInstance() {
        final Fragment fragment = new ToolsFragment();
        final Bundle args = new Bundle(1);
        args.putInt(EXTRA_MODE, MODE_ADDED);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment newAvailableInstance() {
        final Fragment fragment = new ToolsFragment();
        final Bundle args = new Bundle();
        args.putInt(EXTRA_MODE, MODE_AVAILABLE);
        fragment.setArguments(args);
        return fragment;
    }

    /* BEGIN lifecycle */

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
        return inflater.inflate(R.layout.fragment_tools, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolsList();
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
            GodToolsDownloadManager.getInstance(getContext()).addTool(code);
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
    public void onDestroyView() {
        cleanupToolsList();
        super.onDestroyView();
    }

    /* END lifecycle */

    private boolean showDownloading() {
        return mMode == MODE_AVAILABLE;
    }

    @CallSuper
    protected void syncData(final boolean force) {
        super.syncData(force);
        mSyncHelper.sync(GodToolsSyncService.syncTools(getContext(), force));
    }

    private void startLoaders() {
        restartToolsLoader();
    }

    private void restartToolsLoader() {
        getLoaderManager().restartLoader(LOADER_TOOLS, null, mCursorLoaderCallbacks);
    }

    private void setupToolsList() {
        if (mResourcesView != null) {
            mResourcesView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mResourcesView.setHasFixedSize(true);

            mToolsAdapter = new ToolsAdapter(mMode == MODE_ADDED);
            mToolsAdapter.setCallbacks(this);

            // provide an empty list view if required for the current mode
            if (mMode == MODE_ADDED) {
                mToolsHeaderAdapter = new Builder()
                        .layout(R.layout.list_item_none_large_icon)
                        .emptyIcon(R.drawable.ic_find_tools)
                        .emptyAction(R.string.nav_find_tools)
                        .build();
            } else if (mMode == MODE_AVAILABLE) {
                mToolsHeaderAdapter = new Builder()
                        .emptyText(R.string.text_tools_all_installed)
                        .build();
            } else {
                mToolsHeaderAdapter = null;
            }
            if (mToolsHeaderAdapter != null) {
                mToolsHeaderAdapter.setEmptyCallbacks(this);
                mToolsHeaderAdapter.setAdapter(mToolsAdapter);
                mResourcesView.setAdapter(mToolsHeaderAdapter);
            } else {
                mResourcesView.setAdapter(mToolsAdapter);
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
        if (mResourcesView != null) {
            mResourcesView.setAdapter(null);
        }
        mToolsHeaderAdapter = null;
        mToolsAdapter = null;
    }

    // set repetitive TranslationTable objects for primary, parallel, and default
    private static final int TRANS_COUNT = 3;
    private static final int TRANS_PRIMARY = 0;
    private static final int TRANS_PARALLEL = 1;
    private static final int TRANS_DEFAULT = 2;
    static final String[] TRANS_ALIAS = new String[TRANS_COUNT];
    static final Table[] TRANS_TABLE = new Table[TRANS_COUNT];
    static final Join[] TRANS_JOIN = new Join[TRANS_COUNT];
    static final Field[] TRANS_FIELD_LANG = new Field[TRANS_COUNT];

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

    static final String[] TOOLS_PROJECTION = {
            ToolTable.COLUMN_ID,
            ToolTable.COLUMN_CODE,
            ToolTable.COLUMN_TYPE,
            "coalesce(" +
                    TRANS_ALIAS[TRANS_PRIMARY] + "." + TranslationTable.COLUMN_NAME + "," +
                    TRANS_ALIAS[TRANS_DEFAULT] + "." + TranslationTable.COLUMN_NAME + "," +
                    ToolTable.TABLE_NAME + "." + ToolTable.COLUMN_NAME + ") " +
                    "AS " + ToolsAdapter.COL_TITLE,
            "CASE " +
                    "WHEN " + TRANS_ALIAS[TRANS_PRIMARY] + "." + TranslationTable.COLUMN_NAME + " IS NOT NULL THEN " +
                    TRANS_ALIAS[TRANS_PRIMARY] + "." + TranslationTable.COLUMN_LANGUAGE + " " +
                    "WHEN " + TRANS_ALIAS[TRANS_DEFAULT] + "." + TranslationTable.COLUMN_NAME + " IS NOT NULL THEN " +
                    TRANS_ALIAS[TRANS_DEFAULT] + "." + TranslationTable.COLUMN_LANGUAGE + " " +
                    "ELSE NULL END AS " + ToolsAdapter.COL_TITLE_LANGUAGE,
            ToolTable.COLUMN_SHARES,
            ToolTable.COLUMN_PENDING_SHARES,
            ToolTable.COLUMN_ADDED,
            TRANS_ALIAS[TRANS_PRIMARY] + "." + TranslationTable.COLUMN_LANGUAGE + " AS " +
                    ToolsAdapter.COL_PRIMARY_LANGUAGE,
            TRANS_ALIAS[TRANS_PARALLEL] + "." + TranslationTable.COLUMN_LANGUAGE + " AS " +
                    ToolsAdapter.COL_PARALLEL_LANGUAGE,
            TRANS_ALIAS[TRANS_DEFAULT] + "." + TranslationTable.COLUMN_LANGUAGE + " AS " +
                    ToolsAdapter.COL_DEFAULT_LANGUAGE,
            AttachmentTable.TABLE_NAME + "." + AttachmentTable.COLUMN_LOCALFILENAME + " AS " + ToolsAdapter.COL_BANNER
    };
    static final Join TOOLS_JOIN_BANNER =
            ToolTable.SQL_JOIN_BANNER.type("LEFT").andOn(AttachmentTable.SQL_WHERE_DOWNLOADED);

    class CursorLoaderCallbacks extends SimpleLoaderCallbacks<Cursor> {
        @Nullable
        @Override
        public Loader<Cursor> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_TOOLS:
                    final DaoCursorEventBusLoader<Tool> loader = new ToolsCursorLoader(getContext(), args);
                    loader.addEventBusSubscriber(new AttachmentEventBusSubscriber(loader));
                    loader.setProjection(TOOLS_PROJECTION);
                    //noinspection unchecked
                    loader.setJoins(
                            TOOLS_JOIN_BANNER,
                            TRANS_JOIN[TRANS_PRIMARY].andOn(TRANS_FIELD_LANG[TRANS_PRIMARY].eq(mPrimaryLanguage)),
                            TRANS_JOIN[TRANS_PARALLEL].andOn(
                                    TRANS_FIELD_LANG[TRANS_PARALLEL]
                                            .eq(mParallelLanguage != null ? mParallelLanguage : Language.INVALID_CODE)),
                            TRANS_JOIN[TRANS_DEFAULT]
                                    .andOn(TRANS_FIELD_LANG[TRANS_DEFAULT].eq(Settings.getDefaultLanguage()))
                    );
                    final Expression where = ToolTable.FIELD_ADDED.eq(mMode == MODE_ADDED);
                    loader.setWhere(where);
                    loader.setGroupBy(ToolTable.FIELD_CODE);
                    return loader;
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
}
