package com.example.rmatt.crureader;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;

import com.example.rmatt.crureader.bo.GDocument.GDocument;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";
    GDocument gDoc;

    public static final String BASE_XML = "35d83e86-bdaa-4892-93fe-0f33576be2b9.xml"; //"35d83e86-bdaa-4892-93fe-0f33576be2b9.xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        try {
            gDoc = XMLUtil.parseGDocument(this,BASE_XML);
            DocumentPagerAdapter dpa = new DocumentPagerAdapter(getSupportFragmentManager(), gDoc);
            viewPager.setOffscreenPageLimit(1);
            viewPager.setAdapter(dpa);
        } catch (Exception e) {
            e.printStackTrace();
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
        public CharSequence getPageTitle(int position)
        {
            return position + ":";
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            super.destroyItem(container, position, object);
        }
    }

}
