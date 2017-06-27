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
import org.ccci.gto.android.common.db.Join;
import org.ccci.gto.android.common.eventbus.content.DaoCursorEventBusLoader;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.cru.godtools.model.event.content.AttachmentEventBusSubscriber;
import org.cru.godtools.sync.GodToolsSyncService;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.adapter.ToolsAdapter;
import org.keynote.godtools.android.content.ToolsCursorLoader;
import org.keynote.godtools.android.db.Contract.AttachmentTable;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.model.Tool;
import org.keynote.godtools.android.service.GodToolsToolManager;

import butterknife.BindView;

public class ToolsFragment extends BaseFragment implements ToolsAdapter.Callbacks {
    private static final String EXTRA_MODE = ToolsFragment.class.getName() + ".MODE";

    public interface Callbacks {
        void onResourceInfo(long id);

        void onResourceSelect(long id);
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
    public void onToolSelect(final long id) {
        final Callbacks listener = FragmentUtils.getListener(this, Callbacks.class);
        if (listener != null) {
            listener.onResourceSelect(id);
        }
    }

    @Override
    public void onToolInfo(final long id) {
        final Callbacks listener = FragmentUtils.getListener(this, Callbacks.class);
        if (listener != null) {
            listener.onResourceInfo(id);
        }
    }

    @Override
    public void onToolAdd(final long id) {
        GodToolsToolManager.getInstance(getContext()).addTool(id);
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
        getLoaderManager().initLoader(LOADER_TOOLS, null, mCursorLoaderCallbacks);
    }

    private void setupToolsList() {
        if (mResourcesView != null) {
            mResourcesView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mResourcesView.setHasFixedSize(true);

            mToolsAdapter = new ToolsAdapter(mMode == MODE_ADDED);
            mToolsAdapter.setCallbacks(this);
            mResourcesView.setAdapter(mToolsAdapter);

            updateToolsList();
        }
    }

    private void updateToolsList() {
        if (mToolsAdapter != null) {
            mToolsAdapter.swapCursor(mResources);
        }
    }

    private void cleanupToolsList() {
        if (mToolsAdapter != null) {
            mToolsAdapter.setCallbacks(null);
        }
        mToolsAdapter = null;
    }

    static final String[] TOOLS_PROJECTION = {
            ToolTable.COLUMN_ID,
            ToolTable.COLUMN_NAME,
            ToolTable.COLUMN_SHARES,
            ToolTable.COLUMN_ADDED,
            AttachmentTable.TABLE_NAME + "." + AttachmentTable.COLUMN_LOCALFILENAME + " AS " + ToolsAdapter.COL_BANNER
    };
    @SuppressWarnings("unchecked")
    static final Join<Tool, ?>[] TOOLS_JOINS =
            new Join[] {ToolTable.SQL_JOIN_BANNER.type("LEFT").andOn(AttachmentTable.SQL_WHERE_DOWNLOADED)};

    class CursorLoaderCallbacks extends SimpleLoaderCallbacks<Cursor> {
        @Nullable
        @Override
        public Loader<Cursor> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_TOOLS:
                    final DaoCursorEventBusLoader<Tool> loader = new ToolsCursorLoader(getContext(), args);
                    loader.addEventBusSubscriber(new AttachmentEventBusSubscriber(loader));
                    loader.setProjection(TOOLS_PROJECTION);
                    loader.setJoins(TOOLS_JOINS);
                    final Expression where = ToolTable.FIELD_ADDED.eq(mMode == MODE_ADDED);
                    loader.setWhere(where);
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
