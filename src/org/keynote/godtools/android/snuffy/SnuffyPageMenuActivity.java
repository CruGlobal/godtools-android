package org.keynote.godtools.android.snuffy;

import android.app.ListActivity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.utils.LanguagesNotSupportedByDefaultFont;
import org.keynote.godtools.android.utils.Typefaces;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class SnuffyPageMenuActivity extends ListActivity
{
    private static String TAG = "SnuffyPageMenuActivity";
    List<HashMap<String, Object>> mList = new ArrayList<HashMap<String, Object>>(2);
    private boolean mFromAssets;
    private String mLanguageCode;
    private String mDocumentsDir;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.page_menu);

        mLanguageCode = getIntent().getStringExtra("LanguageCode");
        String mPackageName = getIntent().getStringExtra("PackageName");
        SnuffyApplication app = (SnuffyApplication) getApplication();
        mFromAssets = app.languageExistsAsAsset(mPackageName, mLanguageCode);
        mDocumentsDir = app.getDocumentsDir().getPath();

        setTitle(app.mPackageTitle);

        // see also : http://stackoverflow.com/questions/6852876/android-about-listview-and-simpleadapter
        // see also: http://android-developers.blogspot.com.au/2009/02/android-layout-tricks-1.html

        HashMap<String, Object> map;

        // the from array specifies which keys from the map
        // we want to view in our ListView
        String[] from = {"label", "image"};

        // the to array specifies the views from the xml layout
        // on which we want to display the values defined in the from array
        int[] to = {R.id.list1Text, R.id.list1Image};

        Vector<SnuffyPage> pages = app.mPages;
        for (SnuffyPage page : pages)
        {
            map = new HashMap<String, Object>();
            map.put("label", page.mDescription);
            map.put("image", page.mThumbnail);
            mList.add(map);
        }

        SimpleImageAdapter adapter = new SimpleImageAdapter(this, mList, R.layout.list_item_with_icon_and_text, from, to);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        setResult(RESULT_FIRST_USER + position);
        finish();
    }

    private Bitmap getBitmapFromAssetOrFile(Context context, String imageFileName)
    {
        // a path is passed such as: /Packages/kgp/en_US/thumbs/uspagethumb_10.png

        // first the package-specific folder
        String path = imageFileName;
        InputStream isImage = null;
        try
        {
            if (mFromAssets)
                isImage = context.getAssets().open(path, AssetManager.ACCESS_BUFFER); // read into memory since it's not very large
            else
            {
                isImage = new BufferedInputStream(new FileInputStream(mDocumentsDir + "/" + path));
            }
            return BitmapFactory.decodeStream(isImage);

        } catch (IOException e)
        {
            // try the next path instead
        } finally
        {
            if (isImage != null)
            {
                try
                {
                    isImage.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        // next the package-specific folder with a @2x
        path = imageFileName.replace(".png", "@2x.png");
        isImage = null;
        try
        {
            if (mFromAssets)
                isImage = context.getAssets().open(path, AssetManager.ACCESS_BUFFER); // read into memory since it's not very large
            else
            {
                Log.d(TAG, "getBitmapFromAssetOrFile:" + mDocumentsDir + "/" + path);
                isImage = new BufferedInputStream(new FileInputStream(mDocumentsDir + "/" + path));
            }
            return BitmapFactory.decodeStream(isImage);

        } catch (IOException e)
        {
            Log.e(TAG, "Cannot open or read bitmap file: " + imageFileName);
            return null;
        } finally
        {
            if (isImage != null)
            {
                try
                {
                    isImage.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

    }

    private class SimpleImageAdapter extends SimpleAdapter
    {

        public SimpleImageAdapter(Context context,
                                  List<? extends Map<String, ?>> data, int resource,
                                  String[] from, int[] to)
        {
            super(context, data, resource, from, to);
        }

        @Override
        public void setViewImage(@NonNull ImageView v, String value)
        {
            Log.d(TAG, "setViewImage: " + value);

            try
            {
                Bitmap bm = getBitmapFromAssetOrFile(getApplicationContext(), value);
                v.setImageBitmap(bm);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = super.getView(position, convertView, parent);

            if (LanguagesNotSupportedByDefaultFont.contains(mLanguageCode))
            {
                if (view instanceof LinearLayout)
                {
                    LinearLayout layout = (LinearLayout) view;
                    for (int i = 0; i < layout.getChildCount(); i++)
                    {
                        if (layout.getChildAt(i) instanceof TextView)
                        {
                            TextView internalTextView = (TextView) layout.getChildAt(i);
                            internalTextView.setTypeface(Typefaces.get(getApplicationContext(), LanguagesNotSupportedByDefaultFont.getPathToAlternateFont(mLanguageCode)));
                        }
                    }
                }
            }

            return view;
        }
    }
}
