package org.keynote.godtools.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;
import org.keynote.godtools.android.utils.LanguagesNotSupportedByDefaultFont;
import org.keynote.godtools.android.utils.Typefaces;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class SelectLanguagePW extends BaseActionBarActivity implements AdapterView.OnItemClickListener, DownloadTask.DownloadTaskHandler
{
    private final String TAG = getClass().getSimpleName();
    
    ListView mList;
    SharedPreferences settings;
    List<GTLanguage> languageList;

    String primaryLanguage, parallelLanguage;
    String currentLanguage;
    Boolean isTranslator;
    Typeface mAlternateTypeface;
    String languageType;
    Intent returnIntent;
    boolean isMainLang;
    
    LanguageAdapter.ViewHolder currentView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_language);
        mList = (ListView) findViewById(android.R.id.list);
        mList.setCacheColorHint(Color.TRANSPARENT);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar_centered_title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        languageType = getIntent().getStringExtra("languageType");
        setTitle("Select " + languageType);

        TextView titleBar = (TextView) actionBar.getCustomView().findViewById(R.id.titlebar_title);
        titleBar.setText(languageType);
        
        actionBar.setDisplayShowTitleEnabled(true);

        languageList = GTLanguage.getAll(this);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        primaryLanguage = settings.getString(GTLanguage.KEY_PRIMARY, "en");
        parallelLanguage = settings.getString(GTLanguage.KEY_PARALLEL, "");
        isTranslator = settings.getBoolean("TranslatorMode", false);
        
        if (languageType.equalsIgnoreCase("Main Language"))
        {
            currentLanguage = primaryLanguage;
            isMainLang = true;
        }
        else if (languageType.equalsIgnoreCase("Parallel Language"))
        {
            currentLanguage = parallelLanguage;
            isMainLang = false;
            removeLanguageFromList(languageList, primaryLanguage);
        }

        if (!isTranslator)
        {
            Iterator<GTLanguage> i = languageList.iterator();
            for (; i.hasNext();)
            {
                GTLanguage lang = i.next();
                if (lang.isDraft()) i.remove();
            }
        }

        // sort the list alphabetically
        Collections.sort(languageList, new Comparator<GTLanguage>()
        {
            public int compare(GTLanguage result1, GTLanguage result2)
            {
                return result1.getLanguageName().compareTo(result2.getLanguageName());
            }
        });

        handleLanguagesWithAlternateFonts(primaryLanguage);

        setList();
    }
    
    private void downloadLanguage(String langCode)
    {
        
        GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                langCode, "primary", settings.getString("Authorization_Generic", ""), this);
    }
    
    public void setList()
    {
        LanguageAdapter adapter = new LanguageAdapter(this, languageList, mAlternateTypeface);
        Log.i(TAG, "current language: " + currentLanguage);
        adapter.setCurrentLanguage(currentLanguage);
        mList.setAdapter(adapter);
        mList.setOnItemClickListener(this);        
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        GTLanguage gtl = languageList.get(position);
        Log.i(TAG, "Selected: " + gtl.getLanguageName());
        
        returnIntent = new Intent();

        if (isMainLang)
        {
            returnIntent.putExtra("primaryCode", gtl.getLanguageCode());
            // set selected language as new primary
            if (gtl.isDownloaded())
            {
                setAsPrimaryOrParallel(GTLanguage.KEY_PRIMARY, gtl.getLanguageCode());
                currentLanguage = gtl.getLanguageCode();

                setResult(RESULT_CHANGED_PRIMARY, returnIntent);
                
                setList();

            }
            // download and set as primary
            else
            {
                Log.i(TAG, "Download: " + gtl.getLanguageName());

                currentView = (LanguageAdapter.ViewHolder) view.getTag();
                currentView.pbDownloading.setVisibility(View.VISIBLE);

                downloadLanguage(gtl.getLanguageCode());
            }

        }
        else
        {
            returnIntent.putExtra("parallelCode", gtl.getLanguageCode());
            
            // set selected language as parallel
            if (gtl.isDownloaded())
            {
                setAsPrimaryOrParallel(GTLanguage.KEY_PARALLEL, gtl.getLanguageCode());

                setResult(RESULT_CHANGED_PARALLEL, returnIntent);
                
                setList();
            }
            // download and set as parallel
            else
            {
                Log.i(TAG, "Download: " + gtl.getLanguageName());

                LanguageAdapter.ViewHolder viewHolder = (LanguageAdapter.ViewHolder) view.getTag();
                viewHolder.pbDownloading.setVisibility(View.VISIBLE);
                
                downloadLanguage(gtl.getLanguageCode());
            }
        }
    }
    
    private void setAsPrimaryOrParallel(String setAs, String langCode)
    {
        if (GTLanguage.KEY_PRIMARY.equals(setAs))
        {
            if (langCode.equalsIgnoreCase(parallelLanguage))
            {
                setAsPrimaryOrParallel(GTLanguage.KEY_PARALLEL, "");
            }
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(setAs, langCode);
        editor.apply();
    }

    private void removeLanguageFromList(List<GTLanguage> list, String code)
    {
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i).getLanguageCode().equalsIgnoreCase(code))
            {
                list.remove(i);
                break;
            }
        }
    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String langCode, String tag)
    {
        Log.i(TAG, "Download Successful");
        
        SnuffyApplication app = (SnuffyApplication) getApplication();
        GTLanguage gtLanguage = GTLanguage.getLanguage(app.getApplicationContext(), langCode);
        gtLanguage.setDownloaded(true);
        gtLanguage.update(app.getApplicationContext());
        
        if (isMainLang)
        {
            setResult(RESULT_CHANGED_PRIMARY, returnIntent);
            currentLanguage = langCode;
            app.setAppLocale(langCode);
            setAsPrimaryOrParallel(GTLanguage.KEY_PRIMARY, langCode);
        }
        else
        {
            setResult(RESULT_CHANGED_PARALLEL, returnIntent);
            primaryLanguage = langCode;
            setAsPrimaryOrParallel(GTLanguage.KEY_PARALLEL, langCode);
        }
        
        setList();
    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String langCode, String tag)
    {
        Log.i(TAG, "Download Failed");
        
        currentView.pbDownloading.setVisibility(View.INVISIBLE);
        currentView.ivDownloaded.setImageResource(R.drawable.gt4_downloads_erroricon);
        currentView.tvDownload.setText(R.string.retry);
    }

    private class LanguageAdapter extends ArrayAdapter<GTLanguage>
    {

        private Context mContext;
        private LayoutInflater mInflater;
        private List<GTLanguage> mLanguageList;
        private String currentLanguage;
        private Typeface tp;

        public LanguageAdapter(Context context, List<GTLanguage> objects, Typeface typeface)
        {
            super(context, R.layout.languages_list_item, objects);
            this.mContext = context;
            this.mLanguageList = objects;
            this.tp = typeface;
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {

            ViewHolder holder;
            if (convertView == null)
            {
                convertView = mInflater.inflate(R.layout.languages_list_item, parent, false);

                holder = new ViewHolder();
                holder.layout = (RelativeLayout) convertView.findViewById(R.id.content_block);
                holder.tvLanguage = (TextView) convertView.findViewById(R.id.tvLanguageName);
                holder.ivDownloaded = (ImageView) convertView.findViewById(R.id.iv_downloaded);
                holder.tvDownload = (TextView) convertView.findViewById(R.id.tv_download);
                holder.pbDownloading = (ProgressBar) convertView.findViewById(R.id.pb_dowloading);

                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }

            final GTLanguage gtl = mLanguageList.get(position);
            holder.tvLanguage.setTypeface(mAlternateTypeface, Typeface.NORMAL);
            holder.tvLanguage.setText(gtl.getLanguageName());
            
            if (gtl.isDownloaded())
            {
                holder.tvDownload.setText(R.string.delete);
            }
            else
            {
                holder.tvDownload.setText(R.string.download);
            }

            if (gtl.getLanguageCode().equalsIgnoreCase(currentLanguage))
            {
                holder.ivDownloaded.setVisibility(View.VISIBLE);
                holder.layout.setBackgroundColor(getResources().getColor(R.color.smokey));
            }
            else
            {
                holder.ivDownloaded.setVisibility(View.INVISIBLE);
                holder.layout.setBackgroundColor(Color.TRANSPARENT);
            }
            
            holder.tvDownload.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    itemOnClickAction(gtl);
                }
            });

            return convertView;
        }

        private class ViewHolder
        {
            public RelativeLayout layout;
            public TextView tvLanguage;
            public ImageView ivDownloaded;
            public TextView tvDownload;
            public ProgressBar pbDownloading;
        }

        public void setCurrentLanguage(String currentLanguage)
        {
            this.currentLanguage = currentLanguage;
        }
    }

    private void handleLanguagesWithAlternateFonts(String mAppLanguage)
    {
        if (LanguagesNotSupportedByDefaultFont.contains(mAppLanguage))
        {
            mAlternateTypeface = Typefaces.get(getApplication(), LanguagesNotSupportedByDefaultFont.getPathToAlternateFont(mAppLanguage));
        }
        else
        {
            mAlternateTypeface = Typeface.DEFAULT;
        }
    }
    
    private void itemOnClickAction(GTLanguage language)
    {
        // if downloading, check for internet connection;
        if (!language.isDownloaded() && !Device.isConnected(SelectLanguagePW.this))
        {
            Toast.makeText(SelectLanguagePW.this, "No internet connection, resources needs to be downloaded.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!language.isDownloaded())
        {
            Log.i(TAG, "Download");
            downloadLanguage(language.getLanguageCode());
        }
        else
        {
            Log.i(TAG, "Delete");
        }
    }
}
