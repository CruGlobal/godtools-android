package org.keynote.godtools.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.customactivities.ListActionActivity;

import java.util.List;

public class SelectLanguagePW extends ListActionActivity {
    private static final String PREFS_NAME = "GodTools";

    SharedPreferences settings;
    List<GTLanguage> languageList;

    String primaryLanguage, parallelLanguage;
    String currentLanguage;
    boolean isMainLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_language);

        String languageType = getIntent().getStringExtra("languageType");
        setPageTitle("Select " + languageType);

        languageList = GTLanguage.getAll(this);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        primaryLanguage = settings.getString(GTLanguage.KEY_PRIMARY, "en");
        parallelLanguage = settings.getString(GTLanguage.KEY_PARALLEL, "");

        if (languageType.equalsIgnoreCase("Main Language")) {
            currentLanguage = primaryLanguage;
            isMainLang = true;
        } else if (languageType.equalsIgnoreCase("Parallel Language")) {
            currentLanguage = parallelLanguage;
            isMainLang = false;
        }

        LanguageAdapter adapter = new LanguageAdapter(this, languageList);
        adapter.setCurrentLanguage(currentLanguage);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        GTLanguage gtl = languageList.get(position);

        if (isMainLang) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PRIMARY, gtl.getLanguageCode());
            editor.commit();

            if (!gtl.isDownloaded()) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("code", gtl.getLanguageCode());
                setResult(2001, returnIntent);
                finish();
            }


        } else {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PARALLEL, gtl.getLanguageCode());
            editor.commit();

        }

    }

    private class LanguageAdapter extends ArrayAdapter<GTLanguage> {

        private Context mContext;
        private LayoutInflater mInflater;
        private List<GTLanguage> mLanguageList;
        private String currentLanguage;

        public LanguageAdapter(Context context, List<GTLanguage> objects) {
            super(context, R.layout.languages_list_item, objects);
            this.mContext = context;
            this.mLanguageList = objects;
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.languages_list_item, parent, false);

                holder = new ViewHolder();
                holder.tvLanguage = (TextView) convertView.findViewById(R.id.tvLanguageName);
                holder.ivDownload = (ImageView) convertView.findViewById(R.id.ivDownloadLanguage);
                holder.tvCurrentLanguageIndicator = (TextView) convertView.findViewById(R.id.tvCurrentLanguageIndicator);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            GTLanguage gtl = mLanguageList.get(position);
            holder.tvLanguage.setText(gtl.getLanguageName());
            holder.ivDownload.setVisibility(gtl.isDownloaded() ? View.GONE : View.VISIBLE);

            if (gtl.getLanguageCode().equalsIgnoreCase(currentLanguage)) {
                holder.tvLanguage.setTextColor(mContext.getResources().getColor(R.color.settings_listitem_item));
                holder.tvLanguage.setTypeface(holder.tvLanguage.getTypeface(), Typeface.BOLD);
            } else {
                holder.tvLanguage.setTextColor(mContext.getResources().getColor(R.color.gray_60));
                holder.tvLanguage.setTypeface(holder.tvLanguage.getTypeface(), Typeface.NORMAL);
            }


            return convertView;
        }

        private class ViewHolder {
            public TextView tvLanguage;
            public ImageView ivDownload;
            public TextView tvCurrentLanguageIndicator;
        }

        public void setCurrentLanguage(String currentLanguage) {
            this.currentLanguage = currentLanguage;
        }
    }
}
