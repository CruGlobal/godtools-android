package org.keynote.godtools.android.snuffy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import java.util.UUID;

public class SnuffyPage extends SnuffyLayout {
	private static final String TAG = "SnuffyPage";
	
	public String mDescription;
	public String mThumbnail;
	public Activity mCallingActivity; // used to host AlertDialog

    /**
     * SnuffyPage's unique identifier on the GodTools API.  This ID is used when fetching an
     * updated version of just one page while making content edits in the translation tool.
     */
    private UUID pageId;

	private View mCover;
	private View mActivePanel;
	private View mHiddenButton;
	private Runnable mOnRemoveCover;

	public SnuffyPage(Context context) {
		super(context);
		mCover        = null;
		mActivePanel  = null;
		mHiddenButton = null;
	}
	
	public void setCover(View cover) {
		mCover = cover;
        mCover.setOnClickListener(new View.OnClickListener() {						
			@Override
			public void onClick(View v) {				
				SnuffyPage.this.hideActivePanel();
			}
        });
	}
	
	public void onEnterPage() {
		Log.d(TAG, "OnEnterPage");
		hideActivePanel(); // probably don't need this since we can rely on onExitPage having cleaned up after the last time this page was shown.
	}
	
	public void onExitPage() {
		Log.d(TAG, "OnExitPage");
		hideActivePanel();
	}
	
	private void hideActivePanel() {
		if (mCover != null) {
			if (mCover.getVisibility() != View.GONE)
				mCover.setVisibility(View.GONE);	
		}
		if (mActivePanel != null) {
			mActivePanel.clearAnimation();
			mActivePanel.setVisibility(View.GONE);
			mActivePanel = null;			
		}
		if (mHiddenButton != null) {
			mHiddenButton.setVisibility(View.VISIBLE);
			mHiddenButton = null;
		}
		if (mOnRemoveCover != null) {
			new Handler().post(mOnRemoveCover);
			mOnRemoveCover = null;
		}
		requestLayout();
		forceLayout();
		invalidate();
	}
	
	public void showCover(Runnable onRemoveCover, boolean bFullyTransparent) {
		// Set up for active panel to be removed on click anywhere on screen or when user drags to a new page
		mOnRemoveCover = onRemoveCover;
		if (bFullyTransparent)
	       	mCover.setBackgroundColor(Color.TRANSPARENT);
		else
			mCover.setBackgroundColor(Color.argb(51, 0,0,0)); // IOS Uses black with alpha=0.2
        mCover.setVisibility(View.VISIBLE);
        mCover.bringToFront();
        requestLayout();
        forceLayout();
	}
	
	public void showPanel(View panel, View forButton) {
        mActivePanel = panel;
        mHiddenButton = forButton;
        requestLayout();
        forceLayout();
	}

    public UUID getPageId()
    {
        return pageId;
    }

    public void setPageId(UUID pageId)
    {
        this.pageId = pageId;
    }

    public void setPageIdFromFilename(String pageFileName)
    {
        Log.d("SnuffyPage", "PageFileName is: " + pageFileName);
        if(pageFileName == null || !pageFileName.contains(".xml"))
        {
            throw new IllegalArgumentException("pageFileName must not be null and must contain .xml");
        }

        String actualFilename;

        if(pageFileName.contains("/"))
        {
            String[] potentialActualFilenames = pageFileName.split("/");
            // the last one.
            actualFilename = potentialActualFilenames[potentialActualFilenames.length - 1];
        }
        else
        {
            actualFilename = pageFileName;
        }
        Log.d("SnuffyPage", "actualFilename: " + actualFilename);

        if(actualFilename.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\\.xml"))
        {
            Log.d("SnuffyPage", "Verified UUID");
            this.pageId = UUID.fromString(actualFilename.substring(0, actualFilename.indexOf(".xml")));
        }
    }
}
