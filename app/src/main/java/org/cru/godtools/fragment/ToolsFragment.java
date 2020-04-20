package org.cru.godtools.fragment;

import android.app.Activity;
import android.graphics.drawable.NinePatchDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.ccci.gto.android.common.sync.swiperefreshlayout.widget.SwipeRefreshSyncHelper;
import org.cru.godtools.R;
import org.cru.godtools.adapter.BannerHeaderAdapter;
import org.cru.godtools.base.util.LocaleUtils;
import org.cru.godtools.databinding.ToolsFragmentBinding;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.event.ToolUpdateEvent;
import org.cru.godtools.sync.GodToolsSyncServiceKt;
import org.cru.godtools.tutorial.PageSet;
import org.cru.godtools.tutorial.activity.TutorialActivityKt;
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsActionEvent;
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsActionEventKt;
import org.cru.godtools.ui.tools.ToolsAdapter;
import org.cru.godtools.ui.tools.ToolsAdapterCallbacks;
import org.cru.godtools.ui.tools.ToolsFragmentDataModel;
import org.cru.godtools.widget.BannerType;
import org.keynote.godtools.android.db.GodToolsDao;

import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

import static org.cru.godtools.base.Settings.FEATURE_TUTORIAL_TRAINING;

public class ToolsFragment extends BasePlatformFragment implements ToolsAdapterCallbacks {
    private static final String EXTRA_MODE = ToolsFragment.class.getName() + ".MODE";

    public interface Callbacks {
        void onToolInfo(@Nullable String code);

        void onToolSelect(@Nullable String code, @NonNull Tool.Type type, Locale... languages);

        void onNoToolsAvailableAction();
    }

    public static final int MODE_ADDED = 1;
    public static final int MODE_AVAILABLE = 2;

    @Inject
    GodToolsDao mDao;
    @Inject
    GodToolsDownloadManager mDownloadManager;

    // these properties should be treated as final and only set/modified in onCreate()
    /*final*/ int mMode = MODE_ADDED;

    @Nullable
    private ToolsFragmentBinding mBinding;

    @Nullable
    private RecyclerViewDragDropManager mToolsDragDropManager;
    @Nullable
    private RecyclerView.Adapter mToolsDragDropAdapter;
    @Nullable
    private BannerHeaderAdapter mToolsHeaderAdapter;
    @Nullable
    private ToolsAdapter mToolsAdapter;

    public static Fragment newInstance(final int mode) {
        final Fragment fragment = new ToolsFragment();
        final Bundle args = new Bundle(1);
        args.putInt(EXTRA_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    // region Lifecycle
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mMode = args.getInt(EXTRA_MODE, mMode);
        }

        setupDataModel();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @NonNull final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        mBinding = ToolsFragmentBinding.inflate(inflater, container, false);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        inflateEmptyListUi(inflater, savedInstanceState);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupDataBinding();
        setupToolsList();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateVisibleBanner();
    }

    @CallSuper
    public void onSyncData(@NonNull final SwipeRefreshSyncHelper helper, final boolean force) {
        super.onSyncData(helper, force);
        helper.sync(GodToolsSyncServiceKt.syncTools(requireContext(), force));
    }

    @Override
    protected void onUpdateFeatureDiscovery(@NonNull final String feature) {
        super.onUpdateFeatureDiscovery(feature);
        if (FEATURE_TUTORIAL_TRAINING.equals(feature)) {
            updateVisibleBanner();
        }
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
            mDownloadManager.addTool(code);
        }
    }

    @Override
    public void onToolsReordered(final long... ids) {
        if (mDao != null) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
                mDao.updateToolOrder(ids);
                eventBus.post(ToolUpdateEvent.INSTANCE);
            });
        }
    }

    void onEmptyActionClick() {
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
        mBinding = null;
    }
    // endregion Lifecycle

    // region Banners
    private void updateVisibleBanner() {
        if (mToolsHeaderAdapter != null) {
            if (!settings.isFeatureDiscovered(FEATURE_TUTORIAL_TRAINING) && mMode == MODE_ADDED &&
                    PageSet.TRAINING.supportsLocale(LocaleUtils.getDeviceLocale(requireContext()))) {
                mToolsHeaderAdapter.setBanner(BannerType.TUTORIAL_TRAINING);
                mToolsHeaderAdapter.setPrimaryCallback(b -> openTrainingTutorial());
                mToolsHeaderAdapter.setSecondaryCallback(b -> {
                    eventBus.post(new TutorialAnalyticsActionEvent(
                            TutorialAnalyticsActionEventKt.ADOBE_TUTORIAL_HOME_DISMISS)
                    );
                    settings.setFeatureDiscovered(FEATURE_TUTORIAL_TRAINING);
                });
            } else {
                mToolsHeaderAdapter.setBanner(null);
            }
        }
    }

    private void openTrainingTutorial() {
        final Activity activity = getActivity();
        if (activity != null) {
            TutorialActivityKt.startTutorialActivity(activity, PageSet.TRAINING);
        }
    }
    // endregion Banners

    // region Data Model
    private ToolsFragmentDataModel mDataModel;

    private void setupDataModel() {
        mDataModel = new ViewModelProvider(this).get(ToolsFragmentDataModel.class);
        mDataModel.getMode().setValue(mMode);
    }
    // endregion Data Model

    private void setupDataBinding() {
        if (mBinding != null) {
            mBinding.setTools(mDataModel.getTools());
        }
    }

    // region Tools List
    @Nullable
    @BindView(R.id.tools)
    RecyclerView mToolsView;

    @SuppressWarnings("unchecked")
    private void setupToolsList() {
        if (mToolsView != null) {
            mToolsView.setHasFixedSize(false);

            // create base tools adapter
            mToolsAdapter = new ToolsAdapter(this, new ViewModelProvider(this));
            mToolsAdapter.getCallbacks().set(this);
            mDataModel.getTools().observe(getViewLifecycleOwner(), mToolsAdapter);
            RecyclerView.Adapter adapter = mToolsAdapter;

            // configure the DragDrop RecyclerView components (Only for Added tools)
            if (mMode == MODE_ADDED) {
                mToolsView.setItemAnimator(new DraggableItemAnimator());
                mToolsDragDropManager = new RecyclerViewDragDropManager();
                mToolsDragDropManager.setDraggingItemShadowDrawable((NinePatchDrawable) ContextCompat
                        .getDrawable(requireActivity(), R.drawable.material_shadow_z3));
                mToolsDragDropManager.setInitiateOnLongPress(true);
                mToolsDragDropManager.setInitiateOnMove(false);
                mToolsDragDropAdapter = mToolsDragDropManager.createWrappedAdapter(adapter);
                adapter = mToolsDragDropAdapter;
            }

            // configure banner view if required for the current mode
            if (mMode == MODE_ADDED) {
                mToolsHeaderAdapter = new BannerHeaderAdapter();
                mToolsHeaderAdapter.setAdapter(adapter);
                adapter = mToolsHeaderAdapter;
            }

            // attach the correct adapter to the tools RecyclerView
            mToolsView.setAdapter(adapter);

            // handle some post-adapter configuration
            if (mToolsDragDropManager != null) {
                mToolsDragDropManager.attachRecyclerView(mToolsView);
            }

            updateVisibleBanner();
        }
    }

    private void cleanupToolsList() {
        if (mToolsView != null) {
            mToolsView.setItemAnimator(null);
            mToolsView.setAdapter(null);
        }
        if (mToolsDragDropManager != null) {
            mToolsDragDropManager.release();
        }
        WrapperAdapterUtils.releaseAll(mToolsDragDropAdapter);

        mToolsHeaderAdapter = null;
        mToolsDragDropAdapter = null;
        mToolsDragDropManager = null;
        mToolsAdapter = null;
    }
    // endregion Tools List

    // region Empty List UI
    private void inflateEmptyListUi(@NonNull final LayoutInflater inflater, @Nullable final Bundle savedInstanceState) {
        if (mBinding != null) {
            final int layout = mMode == MODE_ADDED ? R.layout.tools_added_empty_ui : R.layout.tools_available_empty_ui;
            final View emptyUi = inflater.inflate(layout, mBinding.emptyListUi);

            // HACK: quick and dirty way to attach an OnClickListener
            final View button = emptyUi.findViewById(R.id.action);
            if (button != null) {
                button.setOnClickListener(v -> onEmptyActionClick());
            }
        }
    }
    // endregion Empty List UI
}
