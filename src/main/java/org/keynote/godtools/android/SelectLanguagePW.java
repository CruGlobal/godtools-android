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

import org.ccci.gto.android.common.util.AsyncTaskCompat;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.dao.DBContract.GTLanguageTable;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.tasks.DeletedPackageRemovalTask;
import org.keynote.godtools.android.utils.Device;
import org.keynote.godtools.renderer.crureader.bo.GPage.Util.TypefaceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.keynote.godtools.android.utils.Constants.AUTH_DRAFT;
import static org.keynote.godtools.android.utils.Constants.ENGLISH_DEFAULT;
import static org.keynote.godtools.android.utils.Constants.KEY_PRIMARY;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.TRANSLATOR_MODE;

public class SelectLanguagePW extends BaseActionBarActivity implements AdapterView.OnItemClickListener, DownloadTask.DownloadTaskHandler
{
    private final String TAG = getClass().getSimpleName();

    private ListView mList;
    private SharedPreferences settings;
    private List<GTLanguage> languageList;

    private String primaryLanguage;
    private String parallelLanguage;
    private String currentLanguage;
    private Boolean isTranslator;
    private String languageType;
    private Intent returnIntent;
    private boolean userIsSelectingPrimaryLanguage;
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

        initializeViewState();

        initializeTitleBar(actionBar);

        Log.i(TAG, "primary: " + primaryLanguage);
        Log.i(TAG, "parallel: " + parallelLanguage);
    }

    /**
     * Sets up (populates) fields in this instance of SelectLanguagePW
     */
    private void initializeViewState()
    {
        app = (SnuffyApplication) getApplication();

        languageType = getIntent().getStringExtra("languageType");

        initializeViewStateFromSettings();

        if (getString(R.string.settings_main_language).equalsIgnoreCase(languageType))
        {
            currentLanguage = primaryLanguage;
            userIsSelectingPrimaryLanguage = true;
        }
        else if (getString(R.string.settings_parallel_language).equalsIgnoreCase(languageType))
        {
            currentLanguage = parallelLanguage;
            userIsSelectingPrimaryLanguage = false;
        }
        // TODO: else???

        prepareLanguageList();

        applyLanguageListToListView();
    }

    private void initializeTitleBar(ActionBar actionBar)
    {
        setTitle(getString(R.string.settings_select) + languageType);

        TextView titleBar = (TextView) actionBar.getCustomView().findViewById(R.id.titlebar_title);
        titleBar.setText(languageType);

        actionBar.setDisplayShowTitleEnabled(true);
    }

    /**
     * Loads all languages, removes draft if non-translator mode, dedups list and sorts alphabetically
     */
    private void prepareLanguageList()
    {
        languageList = GTLanguage.getAll(this, Locale.getDefault());

        if (!isTranslator)
        {
            removeDraftsFromLanguageList();
        }

        // There are sometimes duplicates of languages.
        languageList = removeDuplicates(languageList);

        if(!userIsSelectingPrimaryLanguage)
        {
            removeLanguageFromList(languageList, primaryLanguage);
        }

        // sort the list alphabetically
        Collections.sort(languageList, new Comparator<GTLanguage>()
        {
            public int compare(GTLanguage result1, GTLanguage result2)
            {
                return result1.getLanguageName().compareTo(result2.getLanguageName());
            }
        });
    }

    /**
     * Load relevant fields from the app settings into private fields in this instance.
     */
    private void initializeViewStateFromSettings()
    {
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        primaryLanguage = settings.getString(GTLanguage.KEY_PRIMARY, ENGLISH_DEFAULT);
        parallelLanguage = settings.getString(GTLanguage.KEY_PARALLEL, "");
        isTranslator = settings.getBoolean(TRANSLATOR_MODE, false);
    }

    private void removeDraftsFromLanguageList()
    {
        Iterator<GTLanguage> i = languageList.iterator();
        for (; i.hasNext(); )
        {
            GTLanguage lang = i.next();
            if (lang.isDraft()) i.remove();
        }
    }

    private void applyLanguageListToListView()
    {
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
        GTLanguage selectedLanguage = languageList.get(position);
        Log.i(TAG, "Selected: " + selectedLanguage.getLanguageName());

        setListLocation();

        returnIntent = new Intent();

        if (userIsSelectingPrimaryLanguage)
        {
            returnIntent.putExtra("primaryCode", selectedLanguage.getLanguageCode());

            if (selectedLanguage.isDownloaded())
            {
                storeLanguageCodeInSettings(GTLanguage.KEY_PRIMARY, selectedLanguage.getLanguageCode());

                setResult(RESULT_CHANGED_PRIMARY, returnIntent);

                finish();
            }
            else
            {
                Log.i(TAG, "Download: " + selectedLanguage.getLanguageName());

                currentView = (LanguageAdapter.ViewHolder) view.getTag();
                currentView.tvDownload.setText(R.string.downloading);
                currentView.pbDownloading.setVisibility(View.VISIBLE);

                if(isTranslator)
                {
                    GodToolsApiClient.downloadDrafts((SnuffyApplication) getApplication(),
                            settings.getString(AUTH_DRAFT,""),
                            selectedLanguage.getLanguageCode(),
                            "draft",
                            this);
                }
                else
                {
                    GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                            selectedLanguage.getLanguageCode(),
                            "primary",
                            this);
                }
            }
        }
        else // userIsSelectingParallelLanguage
        {
            returnIntent.putExtra("parallelCode", selectedLanguage.getLanguageCode());

            // set selected language as parallel
            if (selectedLanguage.isDownloaded())
            {
                storeLanguageCodeInSettings(GTLanguage.KEY_PARALLEL, selectedLanguage.getLanguageCode());

                setResult(RESULT_CHANGED_PARALLEL, returnIntent);

                finish();
            }
            // download and set as parallel
            else
            {
                Log.i(TAG, "Download: " + selectedLanguage.getLanguageName());

                currentView = (LanguageAdapter.ViewHolder) view.getTag();
                currentView.tvDownload.setText(R.string.downloading);
                currentView.pbDownloading.setVisibility(View.VISIBLE);

                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                        selectedLanguage.getLanguageCode(),
                        KEY_PRIMARY,
                        this);
            }
        }
    }


    /*
      This is an "intelligent setter".  It will take the value in language code and store it in the device settings as
      either the primary or parallel language code.  If the user is setting the primary language code to what the
      parallel language code is currently stored as, then the parallel language code is cleared out.  Otherwise,
      primary and parallel would be the same, which is rather pointless.
     */
    private void storeLanguageCodeInSettings(String primaryOrParallel, String languageCode)
    {
        if (GTLanguage.KEY_PRIMARY.equals(primaryOrParallel))
        {
            if (languageCode.equalsIgnoreCase(parallelLanguage))
            {
                storeLanguageCodeInSettings(GTLanguage.KEY_PARALLEL, "");
            }
        }

        settings.edit().putString(primaryOrParallel, languageCode).apply();
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

    private void itemOnClickAction(GTLanguage language)
    {
        setListLocation();

        // if downloading, check for internet connection;
        if (!language.isDownloaded() && !Device.isConnected(SelectLanguagePW.this))
        {
            Toast.makeText(SelectLanguagePW.this, getString(R.string.internet_needed), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!language.isDownloaded())
        {
            Log.i(TAG, "Download");

            currentView.pbDownloading.setVisibility(View.VISIBLE);

            returnIntent = new Intent();

            if(userIsSelectingPrimaryLanguage)
            {
                returnIntent.putExtra("primaryCode", language.getLanguageCode());
            }
            else
            {
                returnIntent.putExtra("parallelCode", language.getLanguageCode());
            }

            currentView.tvDownload.setText(R.string.downloading);

            GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                    language.getLanguageCode(),
                    "primary",
                    this);
        }
        else
        {
            Log.i(TAG, "Delete");

            if(language.getLanguageCode().equalsIgnoreCase(primaryLanguage))
            {
                Toast.makeText(getApplicationContext(), R.string.language_delete_primary, Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            if(language.getLanguageCode().equalsIgnoreCase(parallelLanguage))
            {
                returnIntent = new Intent();
                setResult(RESULT_CHANGED_PARALLEL, returnIntent);
                parallelLanguage = null;
                storeLanguageCodeInSettings(GTLanguage.KEY_PARALLEL, null);
            }

            if(!"en".equalsIgnoreCase(language.getLanguageCode()))
            {
                AsyncTaskCompat.execute(new DeletedPackageRemovalTask(language,
                        (SnuffyApplication) getApplication()));
            }

            updateDownloadedStatus(language.getLanguageCode(), false);

            applyLanguageListToListView();
        }
    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String langCode, String tag)
    {
        Log.i(TAG, "Download Successful: " + langCode);

        updateDownloadedStatus(langCode, true);

        if (userIsSelectingPrimaryLanguage)
        {
            setResult(RESULT_CHANGED_PRIMARY, returnIntent);
            storeLanguageCodeInSettings(GTLanguage.KEY_PRIMARY, langCode);
        }
        else
        {
            setResult(RESULT_CHANGED_PARALLEL, returnIntent);
            storeLanguageCodeInSettings(GTLanguage.KEY_PARALLEL, langCode);
        }

        finish();
    }

    private void updateDownloadedStatus(String langCode, boolean downloaded)
    {
        // update value in database
        {
            final GTLanguage language = new GTLanguage();
            language.setLanguageCode(langCode);
            language.setDownloaded(downloaded);
            DBAdapter.getInstance(this).updateAsync(language, GTLanguageTable.COL_DOWNLOADED);
        }

        // update value in local list
        for (GTLanguage languageFromDisplayedLanguageList : languageList)
        {
            if (languageFromDisplayedLanguageList.getLanguageCode().equals(langCode))
            {
                languageFromDisplayedLanguageList.setDownloaded(downloaded);
            }
        }
    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String langCode, String tag)
    {
        Log.i(TAG, "Download Failed");

        if (isTranslator)
        {
            setResult(RESULT_CHANGED_PRIMARY, returnIntent);
            primaryLanguage = langCode;

            storeLanguageCodeInSettings(GTLanguage.KEY_PRIMARY, langCode);
            applyLanguageListToListView();
        }
        else
        {
            currentView.pbDownloading.setVisibility(View.INVISIBLE);
            currentView.ivDownloaded.setImageResource(R.drawable.gt4_downloads_erroricon);
            currentView.tvDownload.setText(R.string.retry);
        }
    }

    private class LanguageAdapter extends ArrayAdapter<GTLanguage>
    {
        private final LayoutInflater mInflater;
        private final List<GTLanguage> mLanguageList;
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
            TypefaceUtils.setTypeface(holder.tvLanguage, primaryLanguage, Typeface.NORMAL);
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
}
