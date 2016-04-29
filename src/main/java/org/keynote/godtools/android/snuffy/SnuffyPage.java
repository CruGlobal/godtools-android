package org.keynote.godtools.android.snuffy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.view.View;

import org.keynote.godtools.android.snuffy.model.GtPage;

public class SnuffyPage extends SnuffyLayout
{
    private static final String TAG = "SnuffyPage";

    @NonNull
    private final GtPage mModel;

    private final SimpleArrayMap<String, SnuffyPage> mChildPages = new SimpleArrayMap<>();

    public String mDescription;
    public String mThumbnail;
    public Activity mCallingActivity; // used to host AlertDialog

    private View mCover;
    private Runnable mOnRemoveCover;

    public SnuffyPage(@NonNull final Context context, @NonNull final GtPage model) {
        super(context);
        mModel = model;
        mCover = null;
    }

    @NonNull
    public GtPage getModel() {
        return mModel;
    }

    public void addChildPage(@NonNull final SnuffyPage page) {
        mChildPages.put(page.getModel().getId(), page);
    }

    @Nullable
    public SnuffyPage getChildPage(@NonNull final String id) {
        return mChildPages.get(id);
    }

    public void setCover(View cover)
    {
        mCover = cover;
        mCover.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SnuffyPage.this.hideActivePanel();
            }
        });
    }

    public void onEnterPage()
    {
        Log.d(TAG, "OnEnterPage");
        hideActivePanel(); // probably don't need this since we can rely on onExitPage having cleaned up after the last time this page was shown.
    }

    public void onExitPage()
    {
        Log.d(TAG, "OnExitPage");
        hideActivePanel();
    }

    private void hideActivePanel()
    {
        if (mCover != null) {
            mCover.setVisibility(View.GONE);
        }

        if (mOnRemoveCover != null)
        {
            new Handler().post(mOnRemoveCover);
            mOnRemoveCover = null;
        }
        requestLayout();
        forceLayout();
        invalidate();
    }

    public void showCover(Runnable onRemoveCover, boolean bFullyTransparent)
    {
        // Set up for active panel to be removed on click anywhere on screen or when user drags to a new page
        mOnRemoveCover = onRemoveCover;
        if (bFullyTransparent)
            mCover.setBackgroundColor(Color.TRANSPARENT);
        else
            mCover.setBackgroundColor(Color.argb(51, 0, 0, 0)); // IOS Uses black with alpha=0.2
        mCover.setVisibility(View.VISIBLE);
        mCover.bringToFront();
        requestLayout();
        forceLayout();
    }
}
