package com.example.rmatt.crureader;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;

import com.example.rmatt.crureader.bo.GDocument.GDocument;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;

public class MainActivity extends FragmentActivity {


    public static final String BASE_XML = "97f17b1f-b76d-40ad-8be4-9a45d3406e70.xml";
    private static final boolean SINGLE_TEST = false;
    private static final String TAG = "MainActivity";
    GDocument gDoc;

    //"dbbe9fdc-6945-4ad2-a77b-62d3c9d96766.xml";

    //1: "97f17b1f-b76d-40ad-8be4-9a45d3406e70.xml";//"35d83e86-bdaa-4892-93fe-0f33576be2b9.xml"; //"35d83e86-bdaa-4892-93fe-0f33576be2b9.xml";
    //2: "dbbe9fdc-6945-4ad2-a77b-62d3c9d96766.xml";
    ViewPager viewPager;
    ViewPager.OnPageChangeListener onPageChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setAllowEnterTransitionOverlap(false);
            getWindow().setAllowReturnTransitionOverlap(false);
        }

        if (!SINGLE_TEST) {
            setContentView(R.layout.activity_main);


            ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
            try {
                gDoc = XMLUtil.parseGDocument(this, BASE_XML);
                DocumentPagerAdapter dpa = new DocumentPagerAdapter(getSupportFragmentManager(), gDoc);

                viewPager.setAdapter(dpa);
                onPageChangeListener = new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        Log.i(TAG, "On Page Selected: " + position);
                        /*
                        Set the singleton to know what page is selected
                         */
                        RenderSingleton.getInstance().curPosition = position;

                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                };
                viewPager.addOnPageChangeListener(onPageChangeListener);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Intent i = new Intent(this, RenderSingleTestActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (viewPager != null && onPageChangeListener != null) {
            viewPager.removeOnPageChangeListener(onPageChangeListener);
        }
    }

    private class DocumentPagerAdapter extends FragmentStatePagerAdapter {

        private GDocument gDoc;

        public DocumentPagerAdapter(FragmentManager fm, GDocument gDoc) {
            super(fm);
            this.gDoc = gDoc;
        }

        @Override
        public Fragment getItem(int position) {
            return SlidePageFragment.create(position, gDoc.pages.get(position).filename);
        }

        @Override
        public int getCount() {
            return gDoc.pages.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return position + ":";
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            super.destroyItem(container, position, object);
        }
    }

}
