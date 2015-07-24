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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;
import org.keynote.godtools.android.utils.LanguagesNotSupportedByDefaultFont;
import org.keynote.godtools.android.utils.Typefaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static org.keynote.godtools.android.utils.Constants.EMPTY_STRING;
import static org.keynote.godtools.android.utils.Constants.ENGLISH_DEFAULT;
import static org.keynote.godtools.android.utils.Constants.LANGUAGE_TYPE;
import static org.keynote.godtools.android.utils.Constants.MAIN_LANGUAGE;
import static org.keynote.godtools.android.utils.Constants.PARALLEL_LANGUAGE;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.RESULT_CHANGED_PARALLEL;
import static org.keynote.godtools.android.utils.Constants.RESULT_CHANGED_PRIMARY;
import static org.keynote.godtools.android.utils.Constants.TRANSLATOR_MODE;

public class SelectLanguagePW extends ActionBarActivity implements AdapterView.OnItemClickListener, DownloadTask.DownloadTaskHandler
{
    private final String TAG = getClass().getSimpleName();

    private ListView mList;
    private SharedPreferences settings;
    private List<GTLanguage> languageList;

    private String primaryLanguage;
    private String parallelLanguage;
    private String currentLanguage;
    private Typeface mAlternateTypeface;
    private String languageType;
    private Intent returnIntent;
    private boolean isMainLang;
    private boolean downloadOnly;
    private int index;
    private int top;


    private SnuffyApplication app;

    private LanguageAdapter.ViewHolder currentView;

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

        languageType = getIntent().getStringExtra(LANGUAGE_TYPE);
        setTitle(getString(R.string.settings_select) + languageType);

        TextView titleBar = (TextView) actionBar.getCustomView().findViewById(R.id.titlebar_title);
        titleBar.setText(languageType);

        actionBar.setDisplayShowTitleEnabled(true);

        languageList = GTLanguage.getAll(this);

        app = (SnuffyApplication) getApplication();
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        primaryLanguage = settings.getString(GTLanguage.KEY_PRIMARY, ENGLISH_DEFAULT);
        parallelLanguage = settings.getString(GTLanguage.KEY_PARALLEL, EMPTY_STRING);
        Boolean isTranslator = settings.getBoolean(TRANSLATOR_MODE, false);

        Log.i(TAG, "primary: " + primaryLanguage);
        Log.i(TAG, "parallel: " + parallelLanguage);

        if (!isTranslator)
        {
            Iterator<GTLanguage> i = languageList.iterator();
            for (; i.hasNext(); )
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
    
    public void setList()
    {
        if (languageType.equalsIgnoreCase(MAIN_LANGUAGE))
        {
            currentLanguage = primaryLanguage;
            isMainLang = true;
        }
        else if (languageType.equalsIgnoreCase(PARALLEL_LANGUAGE))
        {
            currentLanguage = parallelLanguage;
            isMainLang = false;
            removeLanguageFromList(languageList, primaryLanguage);
        }

        // There are sometimes duplicates of languages.
        languageList = removeDuplicates(languageList);

        LanguageAdapter adapter = new LanguageAdapter(this, languageList);
        Log.i(TAG, "current language: " + currentLanguage);
        adapter.setCurrentLanguage(currentLanguage);
        mList.setAdapter(adapter);
        mList.setOnItemClickListener(this);

        mList.setSelectionFromTop(index, top);
    }

    private List<GTLanguage> removeDuplicates(List<GTLanguage> original)
    {
        List<GTLanguage> secondList = new ArrayList<GTLanguage>();
        for (GTLanguage language : original)
        {
            if (!secondList.contains(language)) secondList.add(language);
        }

        return secondList;
    }

    private void setListLocation()
    {
        index = mList.getFirstVisiblePosition();
        View localView = mList.getChildAt(0);
        top = (localView == null) ? 0 : (localView.getTop() - mList.getPaddingTop());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        GTLanguage gtl = languageList.get(position);
        Log.i(TAG, "Selected: " + gtl.getLanguageName());

        setListLocation();

        returnIntent = new Intent();

        if (isMainLang)
        {
            returnIntent.putExtra("primaryCode", gtl.getLanguageCode());
            // set selected language as new primary
            if (gtl.isDownloaded())
            {
                storeLanguageCode(GTLanguage.KEY_PRIMARY, gtl.getLanguageCode());
                primaryLanguage = gtl.getLanguageCode();

                setResult(RESULT_CHANGED_PRIMARY, returnIntent);

                setList();

            }
            // download and set as primary
            else
            {
                Log.i(TAG, "Download: " + gtl.getLanguageName());
                downloadOnly = false;

                currentView = (LanguageAdapter.ViewHolder) view.getTag();
                currentView.tvDownload.setText(R.string.downloading);
                currentView.pbDownloading.setVisibility(View.VISIBLE);

                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                        gtl.getLanguageCode(),
                        "primary",
                        this);
            }
        }
        else
        {
            returnIntent.putExtra("parallelCode", gtl.getLanguageCode());

            // set selected language as parallel
            if (gtl.isDownloaded())
            {
                storeLanguageCode(GTLanguage.KEY_PARALLEL, gtl.getLanguageCode());
                parallelLanguage = gtl.getLanguageCode();

                setResult(RESULT_CHANGED_PARALLEL, returnIntent);

                setList();
            }
            // download and set as parallel
            else
            {
                Log.i(TAG, "Download: " + gtl.getLanguageName());
                downloadOnly = false;

                currentView = (LanguageAdapter.ViewHolder) view.getTag();
                currentView.tvDownload.setText(R.string.downloading);
                currentView.pbDownloading.setVisibility(View.VISIBLE);

                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                        gtl.getLanguageCode(),
                        "primary",
                        this);            }
        }
    }


    /*
      This is an "intelligent setter".  It will take the value in language code and store it in the device settings as
      either the primary or parallel language code.  If the user is setting the primary language code to what the
      parallel language code is currently stored as, then the parallel language code is cleared out.  Otherwise,
      primary and parallel would be the same, which is rather pointless.
     */
    private void storeLanguageCode(String primaryOrParallel, String languageCode)
    {
        if (GTLanguage.KEY_PRIMARY.equals(primaryOrParallel))
        {
            if (languageCode.equalsIgnoreCase(parallelLanguage))
            {
                storeLanguageCode(GTLanguage.KEY_PARALLEL, "");
            }
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(primaryOrParallel, languageCode);
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

    private class LanguageAdapter extends ArrayAdapter<GTLanguage>
    {
        private LayoutInflater mInflater;
        private List<GTLanguage> mLanguageList;
        private String currentLanguage;

        public LanguageAdapter(Context context, List<GTLanguage> objects)
        {
            super(context, R.layout.languages_list_item, objects);
            this.mLanguageList = objects;
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {

            final ViewHolder holder;
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
                    currentView = holder;
                    itemOnClickAction(gtl);
                }
            });

            return convertView;
        }

        public void setCurrentLanguage(String currentLanguage)
        {
            this.currentLanguage = currentLanguage;
        }

        private class ViewHolder
        {
            public RelativeLayout layout;
            public TextView tvLanguage;
            public ImageView ivDownloaded;
            public TextView tvDownload;
            public ProgressBar pbDownloading;
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
        setListLocation();
        
        // if downloading, check for internet connection;
        if (!language.isDownloaded() && !Device.isConnected(SelectLanguagePW.this))
        {
            Toast.makeText(SelectLanguagePW.this, "No internet connection, resources needs to be downloaded.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        currentView.pbDownloading.setVisibility(View.VISIBLE);
        downloadOnly = true;

        if (!language.isDownloaded())
        {
            Log.i(TAG, "Download");
            currentView.tvDownload.setText(R.string.downloading);

            GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                    language.getLanguageCode(),
                    "primary",
                    this);
        }
        else
        {
            Log.i(TAG, "Delete");
            updateDownloadedStatus(language.getLanguageCode(), false);
            DBAdapter adapter = DBAdapter.getInstance(this);
            adapter.deletePackages(language.getLanguageCode(), "live");
            setList();
        }
    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String langCode, String tag)
    {
        Log.i(TAG, "Download Successful: " + langCode);

        updateDownloadedStatus(langCode, true);

        if (!downloadOnly)
        {
            if (isMainLang)
            {
                setResult(RESULT_CHANGED_PRIMARY, returnIntent);
                primaryLanguage = langCode;
                app.setAppLocale(langCode);
                storeLanguageCode(GTLanguage.KEY_PRIMARY, langCode);
            }
            else
            {
                setResult(RESULT_CHANGED_PARALLEL, returnIntent);
                parallelLanguage = langCode;
                storeLanguageCode(GTLanguage.KEY_PARALLEL, langCode);
            }
        }

        setList();
    }

    private void updateDownloadedStatus(String langCode, boolean downloaded)
    {
        if (downloaded)
        {
            GTLanguage gtLanguage = GTLanguage.getLanguage(app.getApplicationContext(), langCode);
            gtLanguage.setDownloaded(true);
            gtLanguage.update(app.getApplicationContext());
        }

        for (GTLanguage language : languageList)
        {
            if (language.getLanguageCode().equals(langCode)) language.setDownloaded(downloaded);
        }
    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String langCode, String tag)
    {
        Log.i(TAG, "Download Failed");

        currentView.pbDownloading.setVisibility(View.INVISIBLE);
        currentView.ivDownloaded.setImageResource(R.drawable.gt4_downloads_erroricon);
        currentView.tvDownload.setText(R.string.retry);
    }
}
