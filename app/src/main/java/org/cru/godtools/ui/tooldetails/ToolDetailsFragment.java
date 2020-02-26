package org.cru.godtools.ui.tooldetails;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayoutUtils;

import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.ccci.gto.android.common.viewpager.view.ChildHeightAwareViewPager;
import org.cru.godtools.R;
import org.cru.godtools.databinding.ToolDetailsFragmentBinding;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.fragment.BaseBindingPlatformFragment;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;
import org.cru.godtools.shortcuts.GodToolsShortcutManager;
import org.cru.godtools.shortcuts.GodToolsShortcutManager.PendingShortcut;
import org.cru.godtools.util.ActivityUtilsKt;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;

import static org.cru.godtools.base.Constants.EXTRA_TOOL;

public class ToolDetailsFragment extends BaseBindingPlatformFragment<ToolDetailsFragmentBinding> {
    public interface Callbacks {
        void onToolAdded();

        void onToolRemoved();
    }

    @Nullable
    private GodToolsDownloadManager mDownloadManager;
    @Nullable
    private GodToolsShortcutManager mShortcutManager;

    // these properties should be treated as final and only set/modified in onCreate()
    @Nullable
    /*final*/ String mToolCode = Tool.INVALID_CODE;

    @Nullable
    @BindView(R.id.detail_view_pager)
    ChildHeightAwareViewPager mViewPager;

    @Nullable
    private Translation mLatestParallelTranslation;

    public static Fragment newInstance(@Nullable final String code) {
        final ToolDetailsFragment fragment = new ToolDetailsFragment();
        final Bundle args = new Bundle(1);
        args.putString(EXTRA_TOOL, code);
        fragment.setArguments(args);
        return fragment;
    }

    public ToolDetailsFragment() {
        super(R.layout.tool_details_fragment);
    }

    // region Lifecycle
    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);
        if (mDownloadManager == null) {
            mDownloadManager = GodToolsDownloadManager.getInstance(context);
        }
        if (mShortcutManager == null) {
            mShortcutManager = GodToolsShortcutManager.getInstance(context);
        }
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final Bundle args = getArguments();
        if (args != null) {
            mToolCode = args.getString(EXTRA_TOOL, mToolCode);
        }

        setupDataModel();
    }

    @Override
    public void onBindingCreated(@NonNull final ToolDetailsFragmentBinding binding,
                                 @Nullable final Bundle savedInstanceState) {
        super.onBindingCreated(binding, savedInstanceState);
        binding.setFragment(this);
        binding.setTool(mDataModel.getTool());
        binding.setBanner(mDataModel.getBanner());
        binding.setPrimaryTranslation(mDataModel.getPrimaryTranslation());
        binding.setDownloadProgress(mDataModel.getDownloadProgress());

        setupOverviewVideo(binding);
        setupViewPager(binding);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_tool_details, menu);
        setupPinShortcutAction(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pin_shortcut:
                final PendingShortcut shortcut = mDataModel.getShortcut().getValue();
                if (mShortcutManager != null && shortcut != null) {
                    mShortcutManager.pinShortcut(shortcut);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onLoadLatestParallelTranslation(@Nullable final Translation translation) {
        mLatestParallelTranslation = translation;
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        destroyPinShortcutAction();
    }
    // endregion Lifecycle

    // region Overview Video
    private void setupOverviewVideo(@NonNull final ToolDetailsFragmentBinding binding) {
        getViewLifecycleOwner().getLifecycle().addObserver(binding.videoBanner);
    }
    // endregion Overview Video

    // region Data Model
    private ToolDetailsFragmentDataModel mDataModel;

    private void setupDataModel() {
        mDataModel = new ViewModelProvider(this).get(ToolDetailsFragmentDataModel.class);
        mDataModel.getToolCode().setValue(mToolCode);
        mDataModel.getParallelTranslation().observe(this, this::onLoadLatestParallelTranslation);
    }
    // endregion Data Model

    // region Data Binding
    public void addTool(@Nullable final String toolCode) {
        if (mDownloadManager != null && toolCode != null) {
            mDownloadManager.addTool(toolCode);
            final Callbacks callbacks = FragmentUtils.getListener(this, Callbacks.class);
            if (callbacks != null) {
                callbacks.onToolAdded();
            }
        }
    }

    public void removeTool(@Nullable final String toolCode) {
        if (mDownloadManager != null && toolCode != null) {
            mDownloadManager.removeTool(toolCode);
            final Callbacks callbacks = FragmentUtils.getListener(this, Callbacks.class);
            if (callbacks != null) {
                callbacks.onToolRemoved();
            }
        }
    }

    public void openTool(@Nullable final Tool tool, @Nullable final Translation primaryTranslation) {
        if (tool != null && tool.getCode() != null) {
            final Locale primaryLanguage =
                    primaryTranslation != null ? primaryTranslation.getLanguageCode() : Locale.ENGLISH;
            Locale parallelLanguages = mLatestParallelTranslation != null ?
                    mLatestParallelTranslation.getLanguageCode() : null;
            if (parallelLanguages != null) {
                ActivityUtilsKt.openToolActivity(
                        requireActivity(), tool.getCode(), tool.getType(), primaryLanguage, parallelLanguages);
            } else {
                ActivityUtilsKt.openToolActivity(requireActivity(), tool.getCode(), tool.getType(), primaryLanguage);
            }
        }
    }
    // endregion Data Binding

    // region Pin Shortcut
    @Nullable
    private Observer<PendingShortcut> mPinShortcutObserver;

    private void setupPinShortcutAction(@NonNull final Menu menu) {
        final MenuItem pinShortcutAction = menu.findItem(R.id.action_pin_shortcut);
        if (pinShortcutAction != null) {
            mPinShortcutObserver = shortcut -> pinShortcutAction.setVisible(shortcut != null);
            mDataModel.getShortcut().observe(this, mPinShortcutObserver);
        }
    }

    private void destroyPinShortcutAction() {
        if (mPinShortcutObserver != null) {
            mDataModel.getShortcut().removeObserver(mPinShortcutObserver);
        }
        mPinShortcutObserver = null;
    }
    // endregion Pin Shortcut

    // region ViewPager
    private void setupViewPager(@NonNull final ToolDetailsFragmentBinding binding) {
        binding.detailViewPager
                .setAdapter(new ToolDetailsPagerAdapter(requireContext(), getViewLifecycleOwner(), mDataModel));
        mDataModel.getAvailableLanguages().observe(getViewLifecycleOwner(), it -> TabLayoutUtils
                .notifyPagerAdapterChanged(binding.detailTabLayout));
        binding.detailTabLayout.setupWithViewPager(binding.detailViewPager, true);
    }
    // endregion ViewPager
}
