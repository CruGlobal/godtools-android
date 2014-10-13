package org.keynote.godtools.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.utils.Device;
import org.keynote.godtools.android.utils.LanguagesNotSupportedByDefaultFont;
import org.keynote.godtools.android.utils.Typefaces;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SelectLanguagePW extends BaseActionBarActivity implements AdapterView.OnItemClickListener {

    ListView mList;
    SharedPreferences settings;
    List<GTLanguage> languageList;

    String primaryLanguage, parallelLanguage;
    String currentLanguage;
    Boolean isTranslator;
    Typeface mAlternateTypeface;
    boolean isMainLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_language);
        mList = (ListView) findViewById(android.R.id.list);
        mList.setCacheColorHint(Color.TRANSPARENT);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        String languageType = getIntent().getStringExtra("languageType");
        setTitle("Select " + languageType);

        languageList = GTLanguage.getAll(this);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        primaryLanguage = settings.getString(GTLanguage.KEY_PRIMARY, "en");
        parallelLanguage = settings.getString(GTLanguage.KEY_PARALLEL, "");
        isTranslator = settings.getBoolean("TranslatorMode", false);

        handleLanguagesWithAlternateFonts(primaryLanguage);

        if (languageType.equalsIgnoreCase("Main Language")) {
            currentLanguage = primaryLanguage;
            isMainLang = true;
        } else if (languageType.equalsIgnoreCase("Parallel Language")) {
            currentLanguage = parallelLanguage;
            isMainLang = false;
            removeLanguageFromList(languageList, primaryLanguage);
        }

        Log.i("Translator: ", isTranslator.toString());

        if (!isTranslator)
        {
           for (GTLanguage language : languageList)
           {
               if (language.isDraft())
               {
                   Log.i(language.getLanguageCode(), "draft: " + language.isDraft());
                   //removeLanguageFromList(languageList, language.getLanguageCode());
               }
           }
        }

        // sort the list alphabetically
        Collections.sort(languageList, new Comparator<GTLanguage>() {
            public int compare(GTLanguage result1, GTLanguage result2) {
                return result1.getLanguageName().compareTo(result2.getLanguageName());
            }
        });

        LanguageAdapter adapter = new LanguageAdapter(this, languageList, mAlternateTypeface);
        adapter.setCurrentLanguage(currentLanguage);
        mList.setAdapter(adapter);
        mList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        GTLanguage gtl = languageList.get(position);
        if (!gtl.isDownloaded() && !Device.isConnected(SelectLanguagePW.this)) {
            Toast.makeText(SelectLanguagePW.this, "No internet connection, resources needs to be downloaded.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gtl.getLanguageCode().equalsIgnoreCase(currentLanguage)) {
            finish();
        }

        Intent returnIntent = new Intent();

        if (isMainLang) {
            returnIntent.putExtra("primaryCode", gtl.getLanguageCode());
            if (gtl.isDownloaded()) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(GTLanguage.KEY_PRIMARY, gtl.getLanguageCode());

                if (gtl.getLanguageCode().equalsIgnoreCase(parallelLanguage)) {
                    editor.putString(GTLanguage.KEY_PARALLEL, "");
                }
                editor.commit();

                setResult(RESULT_CHANGED_PRIMARY, returnIntent);

            } else {
                setResult(RESULT_DOWNLOAD_PRIMARY, returnIntent);

            }

        } else {

            returnIntent.putExtra("parallelCode", gtl.getLanguageCode());

            if (gtl.isDownloaded()) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(GTLanguage.KEY_PARALLEL, gtl.getLanguageCode());
                editor.commit();

                setResult(RESULT_CHANGED_PARALLEL, returnIntent);

            } else {

                setResult(RESULT_DOWNLOAD_PARALLEL, returnIntent);

            }

        }

        finish();
    }

    private void removeLanguageFromList(List<GTLanguage> list, String code) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getLanguageCode().equalsIgnoreCase(code)) {
                list.remove(i);
                break;
            }
        }
    }

    private class LanguageAdapter extends ArrayAdapter<GTLanguage> {

        private Context mContext;
        private LayoutInflater mInflater;
        private List<GTLanguage> mLanguageList;
        private String currentLanguage;
        private Typeface tp;

        public LanguageAdapter(Context context, List<GTLanguage> objects, Typeface typeface) {
            super(context, R.layout.languages_list_item, objects);
            this.mContext = context;
            this.mLanguageList = objects;
            this.tp = typeface;
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

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            GTLanguage gtl = mLanguageList.get(position);
            holder.tvLanguage.setTypeface(mAlternateTypeface, Typeface.NORMAL);
            holder.tvLanguage.setTextColor(mContext.getResources().getColor(R.color.gray_60));
            holder.tvLanguage.setText(gtl.getLanguageName());
            holder.ivDownload.setVisibility(gtl.isDownloaded() ? View.GONE : View.VISIBLE);

            if (gtl.getLanguageCode().equalsIgnoreCase(currentLanguage)) {
                holder.tvLanguage.setTextColor(mContext.getResources().getColor(R.color.settings_listitem_item));
                holder.tvLanguage.setTypeface(mAlternateTypeface, Typeface.BOLD);
            }

            return convertView;
        }

        private class ViewHolder {
            public TextView tvLanguage;
            public ImageView ivDownload;
        }

        public void setCurrentLanguage(String currentLanguage) {
            this.currentLanguage = currentLanguage;
        }
    }

    private void handleLanguagesWithAlternateFonts(String mAppLanguage) {
        if (LanguagesNotSupportedByDefaultFont.contains(mAppLanguage)) {
            mAlternateTypeface = Typefaces.get(getApplication(), LanguagesNotSupportedByDefaultFont.getPathToAlternateFont(mAppLanguage));
        } else {
            mAlternateTypeface = Typeface.DEFAULT;
        }
    }
}
