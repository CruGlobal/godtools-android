package org.cru.godtools.ui.tooldetails;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayoutUtils;

import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.ccci.gto.android.common.viewpager.view.ChildHeightAwareViewPager;
import org.cru.godtools.R;
import org.cru.godtools.databinding.ToolDetailsFragmentBinding;
import org.cru.godtools.databinding.ToolDetailsPageDescriptionBinding;
import org.cru.godtools.databinding.ToolDetailsPageLanguagesBinding;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.fragment.BaseBindingPlatformFragment;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;
import org.cru.godtools.shortcuts.GodToolsShortcutManager;
import org.cru.godtools.shortcuts.GodToolsShortcutManager.PendingShortcut;
import org.cru.godtools.util.ActivityUtilsKt;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.library.baseAdapters.BR;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;
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
    private MenuItem mPinShortcutItem;

    @Nullable
    @BindView(R.id.detail_view_pager)
    ChildHeightAwareViewPager mViewPager;

    @Nullable
    private Tool mTool;
    @Nullable
    private Translation mLatestPrimaryTranslation;
    @Nullable
    private Translation mLatestParallelTranslation;
    @NonNull
    private List<Locale> mLanguages = Collections.emptyList();
    @Nullable
    private PendingShortcut mPendingToolShortcut;

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
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_tool_details, menu);
        mPinShortcutItem = menu.findItem(R.id.action_pin_shortcut);
        updatePinShortcutAction();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pin_shortcut:
                if (mShortcutManager != null && mPendingToolShortcut != null) {
                    mShortcutManager.pinShortcut(mPendingToolShortcut);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onLoadTool(@Nullable final Tool tool) {
        mTool = tool;
        updatePinShortcutAction();
    }

    void onLoadLatestPrimaryTranslation(@Nullable final Translation translation) {
        mLatestPrimaryTranslation = translation;
    }

    void onLoadLatestParallelTranslation(@Nullable final Translation translation) {
        mLatestParallelTranslation = translation;
    }

    void onLoadAvailableLanguages(@Nullable final List<Locale> locales) {
        mLanguages = locales != null ? locales : Collections.emptyList();
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        mPinShortcutItem = null;
    }
    // endregion Lifecycle

    private void updatePinShortcutAction() {
        if (mPinShortcutItem != null) {
            if (mShortcutManager != null && mShortcutManager.canPinToolShortcut(mTool)) {
                // get a pending shortcut if we don't have one yet
                if (mPendingToolShortcut == null) {
                    mPendingToolShortcut = mShortcutManager.getPendingToolShortcut(mToolCode);
                }

                mPinShortcutItem.setVisible(mPendingToolShortcut != null);
            } else {
                mPinShortcutItem.setVisible(false);
            }
        }
    }

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
        mDataModel.getTool().observe(this, this::onLoadTool);
        mDataModel.getPrimaryTranslation().observe(this, this::onLoadLatestPrimaryTranslation);
        mDataModel.getParallelTranslation().observe(this, this::onLoadLatestParallelTranslation);
        mDataModel.getAvailableLanguages().observe(this, this::onLoadAvailableLanguages);
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

    public void openTool(@Nullable final Tool tool) {
        if (tool != null && tool.getCode() != null) {
            Locale primaryLanguage =
                    mLatestPrimaryTranslation != null ? mLatestPrimaryTranslation.getLanguageCode() :
                            Locale.ENGLISH;
            Locale parallelLanguages = mLatestParallelTranslation != null ?
                    mLatestParallelTranslation.getLanguageCode() : null;
            if (parallelLanguages != null) {
                ActivityUtilsKt.openToolActivity(
                        requireActivity(),
                        tool.getCode(),
                        tool.getType(),
                        primaryLanguage,
                        parallelLanguages);
            } else {
                ActivityUtilsKt.openToolActivity(
                        requireActivity(),
                        tool.getCode(),
                        tool.getType(),
                        primaryLanguage);
            }
        }
    }
    // endregion Data Binding

    // region ViewPager
    private void setupViewPager(@NonNull final ToolDetailsFragmentBinding binding) {
        binding.detailViewPager.setAdapter(new ToolDetailsAdapter(getViewLifecycleOwner()));
        mDataModel.getAvailableLanguages().observe(getViewLifecycleOwner(), it -> TabLayoutUtils
                .notifyPagerAdapterChanged(binding.detailTabLayout));
        binding.detailTabLayout.setupWithViewPager(binding.detailViewPager, true);
    }

    class ToolDetailsAdapter extends PagerAdapter {
        final LifecycleOwner mLifecycleOwner;

        ToolDetailsAdapter(final LifecycleOwner lifecycleOwner) {
            mLifecycleOwner = lifecycleOwner;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull final ViewGroup container, int position) {
            final ViewDataBinding binding;
            switch (position) {
                case 0:
                    binding = ToolDetailsPageDescriptionBinding
                            .inflate(LayoutInflater.from(container.getContext()), container, true);
                    binding.setVariable(BR.tool, mDataModel.getTool());
                    binding.setVariable(BR.translation, mDataModel.getPrimaryTranslation());
                    break;
                case 1:
                    binding = ToolDetailsPageLanguagesBinding
                            .inflate(LayoutInflater.from(container.getContext()), container, true);
                    binding.setVariable(BR.languages, mDataModel.getAvailableLanguages());
                    break;
                default:
                    throw new IllegalArgumentException("page " + position + " is not a valid page");
            }
            binding.setLifecycleOwner(mLifecycleOwner);
            return binding;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position,
                                @NonNull Object object) {
            container.removeView(((ViewDataBinding) object).getRoot());
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(final int position) {
            switch (position) {
                case 0:
                    return getString(R.string.label_tools_about);
                case 1:
                    final List<Locale> languages = mDataModel.getAvailableLanguages().getValue();
                    final int count = languages != null ? languages.size() : 0;
                    return getResources()
                            .getQuantityString(R.plurals.label_tools_languages, count, count);
            }
            return "";
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return DataBindingUtil.findBinding(view) == object;
        }
    }
    // endregion ViewPager
}
