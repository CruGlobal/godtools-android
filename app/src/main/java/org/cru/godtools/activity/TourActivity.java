package org.cru.godtools.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.ccci.gto.android.common.util.BundleUtils;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.fragment.BaseFragment;

import butterknife.OnClick;
import butterknife.Optional;

import static android.support.v4.app.ActivityOptionsCompat.makeCustomAnimation;

public class TourActivity extends AppCompatActivity {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    enum Page {
        TOOLS(R.layout.fragment_tour_tools), LANGUAGES(R.layout.fragment_tour_languages);

        static final Page DEFAULT = TOOLS;

        @LayoutRes
        final int mLayout;

        Page(@LayoutRes final int layout) {
            mLayout = layout;
        }
    }

    public static void start(@NonNull final Context context) {
        ContextCompat.startActivity(context, new Intent(context, TourActivity.class),
                                    makeCustomAnimation(context, R.anim.activity_fade_in, R.anim.activity_fade_out)
                                            .toBundle());
    }

    public static void startForResult(@NonNull final Activity activity, final int requestCode) {
        ActivityCompat.startActivityForResult(activity, new Intent(activity, TourActivity.class), requestCode,
                                              makeCustomAnimation(activity, R.anim.activity_fade_in,
                                                                  R.anim.activity_fade_out).toBundle());
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showInitialPageIfNecessary();
    }

    /* END lifecycle */

    @Nullable
    private Page getPage(@Nullable final Fragment fragment) {
        if (fragment instanceof TourFragment) {
            return ((TourFragment) fragment).mPage;
        }
        return null;
    }

    @MainThread
    private void showInitialPageIfNecessary() {
        // short-circuit if there is currently an active page
        final Page current = getPage(getSupportFragmentManager().findFragmentByTag(TAG_MAIN_FRAGMENT));
        if (current != null) {
            return;
        }

        // update the displayed fragment
        showPage(Page.DEFAULT, false);
    }

    @MainThread
    private void showPage(@NonNull final Page page, final boolean addToBackStack) {
        final FragmentManager fm = getSupportFragmentManager();

        // short-circuit if this page is the currently selected page
        final Page current = getPage(fm.findFragmentByTag(TAG_MAIN_FRAGMENT));
        if (current == page) {
            return;
        }

        // update the displayed fragment
        final FragmentTransaction tx = fm.beginTransaction()
                .replace(R.id.frame, TourFragment.newInstance(page), TAG_MAIN_FRAGMENT);
        if (addToBackStack) {
            tx.addToBackStack(TAG_MAIN_FRAGMENT);
        }
        tx.commit();
    }

    public static final class TourFragment extends BaseFragment {
        private static final String EXTRA_PAGE = TourFragment.class.getName() + ".PAGE";

        // these properties should be treated as final and only set/modified in onCreate()
        @NonNull
        /*final*/ Page mPage = Page.TOOLS;

        public static TourFragment newInstance(@NonNull final Page page) {
            final Bundle args = new Bundle(1);
            BundleUtils.putEnum(args, EXTRA_PAGE, page);
            final TourFragment fragment = new TourFragment();
            fragment.setArguments(args);
            return fragment;
        }

        /* BEGIN lifecycle */

        @Override
        public void onCreate(@Nullable final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            final Bundle args = getArguments();
            if (args != null) {
                mPage = BundleUtils.getEnum(args, Page.class, EXTRA_PAGE, mPage);
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                                 @Nullable final Bundle savedInstanceState) {
            return inflater.inflate(mPage.mLayout, container, false);
        }

        /* END lifecycle */

        @Optional
        @OnClick(R.id.action_tour_languages)
        void showPageLanguages() {
            final TourActivity activity = FragmentUtils.getListener(this, TourActivity.class);
            if (activity != null) {
                activity.showPage(Page.LANGUAGES, true);
            }
        }

        @Optional
        @OnClick(R.id.action_tour_close)
        void closeTour() {
            final TourActivity activity = FragmentUtils.getListener(this, TourActivity.class);
            if (activity != null) {
                activity.finish();
                activity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
            }
        }
    }
}
