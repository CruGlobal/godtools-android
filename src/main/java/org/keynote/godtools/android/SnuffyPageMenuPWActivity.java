package org.keynote.godtools.android;

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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.TypefaceUtils;
import org.keynote.godtools.renderer.crureader.bo.GDocument.GDocumentPage;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import org.keynote.godtools.renderer.crureader.bo.GPage.Util.FileUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@TODO: look into this
public class SnuffyPageMenuPWActivity extends ListActivity {
    private static String TAG = "SnuffyPageMenuActivity";
    List<HashMap<String, Object>> mList = new ArrayList<HashMap<String, Object>>(2);
    private boolean mFromAssets;
    private String mLanguageCode;
    private String mFilesDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.page_menu);

        mLanguageCode = getIntent().getStringExtra("LanguageCode");
        SnuffyApplication app = (SnuffyApplication) getApplication();
        mFromAssets = false;
        mFilesDir = FileUtils.getResourcesDir().getPath();

        setTitle(RenderSingleton.getInstance().getGDocument().packagename.content);

        HashMap<String, Object> map;

        // the from array specifies which keys from the map
        // we want to view in our ListView
        String[] from = {"label", "image"};

        // the to array specifies the views from the xml layout
        // on which we want to display the values defined in the from array
        int[] to = {R.id.list1Text, R.id.list1Image};

        List<GDocumentPage> pages = RenderSingleton.getInstance().getGDocument().documentPages;
        if (pages != null) {
            for (GDocumentPage page : pages) {
                map = new HashMap<>();
                map.put("label", page.content);
                map.put("image", page.thumb);
                mList.add(map);
            }
        }

        SimpleImageAdapter adapter = new SimpleImageAdapter(this, mList, R.layout.list_item_with_icon_and_text, from, to);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        setResult(RESULT_FIRST_USER + position);
        finish();
    }

    //TODO: rework this
    private Bitmap getBitmapFromAssetOrFile(Context context, String imageFileName) {
        // a path is passed such as: /Packages/kgp/en_US/thumbs/uspagethumb_10.png

        // first the package-specific folder
        String path = imageFileName;
        InputStream isImage = null;
        try {
            if (mFromAssets)
                isImage = context.getAssets().open(path, AssetManager.ACCESS_BUFFER); // read into memory since it's not very large
            else {
                isImage = new BufferedInputStream(new FileInputStream(mFilesDir + "/" + path));
            }
            return BitmapFactory.decodeStream(isImage);

        } catch (IOException e) {
            // try the next path instead
        } finally {
            if (isImage != null) {
                try {
                    isImage.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // next the package-specific folder with a @2x
        path = imageFileName.replace(".png", "@2x.png");
        isImage = null;
        try {
            if (mFromAssets)
                isImage = context.getAssets().open(path, AssetManager.ACCESS_BUFFER); // read into memory since it's not very large
            else {
                Log.d(TAG, "getBitmapFromAssetOrFile:" + mFilesDir + "/" + path);
                isImage = new BufferedInputStream(new FileInputStream(mFilesDir + "/" + path));
            }
            return BitmapFactory.decodeStream(isImage);

        } catch (IOException e) {
            Log.e(TAG, "Cannot open or read bitmap file: " + imageFileName);
            return null;
        } finally {
            if (isImage != null) {
                try {
                    isImage.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class SimpleImageAdapter extends SimpleAdapter {

        public SimpleImageAdapter(Context context,
                                  List<? extends Map<String, ?>> data, int resource,
                                  String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public void setViewImage(@NonNull ImageView v, String value) {
            Log.d(TAG, "setViewImage: " + value);

            try {
                Bitmap bm = getBitmapFromAssetOrFile(getApplicationContext(), value);
                v.setImageBitmap(bm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            if (view instanceof ViewGroup) {
                ViewGroup layout = (ViewGroup) view;
                for (int i = 0; i < layout.getChildCount(); i++) {
                    if (layout.getChildAt(i) instanceof TextView) {
                        TypefaceUtils.setTypeface((TextView) layout.getChildAt(i), mLanguageCode);
                    }
                }
            }

            return view;
        }
    }
}
