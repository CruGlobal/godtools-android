package org.keynote.godtools.android.fragment;

import android.database.Cursor;
import android.os.Bundle;
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
import org.ccci.gto.android.common.db.support.v4.content.DaoCursorLoader;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.adapter.ResourcesAdapter;
import org.keynote.godtools.android.content.ResourcesCursorLoader;
import org.keynote.godtools.android.db.Contract.ResourceTable;
import org.keynote.godtools.android.model.Tool;
import org.keynote.godtools.android.service.GodToolsResourceManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ToolsFragment extends Fragment implements ResourcesAdapter.Callbacks {
    private static final String EXTRA_MODE = ToolsFragment.class.getName() + ".MODE";

    public interface Callbacks {
        void onResourceInfo(long id);

        void onResourceSelect(long id);
    }

    private static final int MODE_ADDED = 1;
    private static final int MODE_AVAILABLE = 2;

    private static final int LOADER_RESOURCES = 101;

    private final CursorLoaderCallbacks mCursorLoaderCallbacks = new CursorLoaderCallbacks();

    // these properties should be treated as final and only set/modified in onCreate()
    /*final*/ int mMode = MODE_ADDED;

    @Nullable
    Unbinder mButterKnife;

    @Nullable
    @BindView(R.id.resources)
    RecyclerView mResourcesView;
    @Nullable
    private ResourcesAdapter mResourcesAdapter;

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
        return inflater.inflate(R.layout.fragment_resources, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mButterKnife = ButterKnife.bind(this, view);
        setupResourcesList();
    }

    void onLoadResources(@Nullable final Cursor cursor) {
        mResources = cursor;
        updateResourcesList();
    }

    @Override
    public void onResourceSelect(final long id) {
        final Callbacks listener = FragmentUtils.getListener(this, Callbacks.class);
        if (listener != null) {
            listener.onResourceSelect(id);
        }
    }

    @Override
    public void onResourceInfo(final long id) {
        final Callbacks listener = FragmentUtils.getListener(this, Callbacks.class);
        if (listener != null) {
            listener.onResourceInfo(id);
        }
    }

    @Override
    public void onResourceAdd(final long id) {
        GodToolsResourceManager.getInstance(getContext()).addResource(id);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cleanupResourcesList();
        if (mButterKnife != null) {
            mButterKnife.unbind();
        }
    }

    /* END lifecycle */

    private boolean showDownloading() {
        return mMode == MODE_AVAILABLE;
    }

    private void startLoaders() {
        getLoaderManager().initLoader(LOADER_RESOURCES, null, mCursorLoaderCallbacks);
    }

    private void setupResourcesList() {
        if (mResourcesView != null) {
            mResourcesView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mResourcesView.setHasFixedSize(true);

            mResourcesAdapter = new ResourcesAdapter(mMode == MODE_ADDED);
            mResourcesAdapter.setCallbacks(this);
            mResourcesView.setAdapter(mResourcesAdapter);

            updateResourcesList();
        }
    }

    private void updateResourcesList() {
        if (mResourcesAdapter != null) {
            mResourcesAdapter.swapCursor(mResources);
        }
    }

    private void cleanupResourcesList() {
        if (mResourcesAdapter != null) {
            mResourcesAdapter.setCallbacks(null);
        }
        mResourcesAdapter = null;
    }

    class CursorLoaderCallbacks extends SimpleLoaderCallbacks<Cursor> {
        @Nullable
        @Override
        public Loader<Cursor> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_RESOURCES:
                    final DaoCursorLoader<Tool> loader = new ResourcesCursorLoader(getContext(), args);
                    final Expression where = ResourceTable.FIELD_ADDED.eq(mMode == MODE_ADDED);
                    loader.setWhere(where);
                    return loader;
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Cursor> loader, @Nullable final Cursor cursor) {
            switch (loader.getId()) {
                case LOADER_RESOURCES:
                    onLoadResources(cursor);
                    break;
            }
        }
    }
}
