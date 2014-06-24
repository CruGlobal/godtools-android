package org.keynote.godtools.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;

import org.keynote.godtools.android.adapters.LanguageListAdapter;
import org.keynote.godtools.android.business.Language;
import org.keynote.godtools.android.customactivities.ListActionActivity;

import java.util.ArrayList;
import java.util.Random;

public class SelectLanguage extends ListActionActivity implements AdapterView.OnItemClickListener {

    public static final String LOGTAG = "Settings";
    public static final String PREFNAME = "GodTools";

    //    final String ENGLISH_LANGUAGE = "english";
    final String TITLE_LANGUAGE = "Select Main Language";
    final String TITLE_PARALLEL_LANGUAGE = "Select Parallel Language";

    String[] concatLanguages;


    ArrayList<Language> languages;
    LanguageListAdapter adapter;
    String selectedLanguageType;

    AlphaAnimation fadeOutLanguage;
    AlphaAnimation fadeInLanguage;
    AlphaAnimation fadeOutCurrentIndicator;
    AlphaAnimation fadeInCurrentIndicator;
    boolean currentLanguageAnimating = false;

    int currentLanguageIndex = -1;
    int englishLanguageIndex = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_language);

        initialize();

    }

    private void initialize() {

        prepareData();
        getPreferences();
        initializeLanguages();
        createAdapter();
        createAnimations();

        getListView().setOnItemClickListener(this);

    }

    private void prepareData() {
        concatLanguages = getResources().getStringArray(R.array.settings_list_languages);

//        langNamesArray = new String[concatLanguages.length];
//        langCodesArray = new String[concatLanguages.length];
//        for (int i = 0; i < concatLanguages.length; i++) {
//            String temp[] = concatLanguages[i].split(Settings2.SEPARATOR);
//            langCodesArray[i] = temp[0];
//            langNamesArray[i] = temp[1];
//        }

        languages = new ArrayList<Language>();
    }

    private void getPreferences() {
        selectedLanguageType = getIntent().getStringExtra(Settings2.SELECTED_LANGUAGE_TYPE);
        //set the actionbar title
        setPageTitle(selectedLanguageType.equalsIgnoreCase(Settings2.PREF_MAIN_LANGUAGE) ? TITLE_LANGUAGE : TITLE_PARALLEL_LANGUAGE);

        //compare strings instead of indeces to avoid errors when a new language is added
        SharedPreferences pref = getSharedPreferences(PREFNAME, MODE_PRIVATE);
        String currentConcatLanguage = pref.getString(selectedLanguageType, Settings2.DEFAULT_LANGUAGE);

        //get the index of the current language
        for (int i = 0; i < concatLanguages.length; i++) {
            String concatLanguage = concatLanguages[i].trim();

            if (concatLanguage.equalsIgnoreCase(currentConcatLanguage)) {
                currentLanguageIndex = i;
//                break;
            }

            if (concatLanguage.equalsIgnoreCase(Settings2.DEFAULT_LANGUAGE)) {
                englishLanguageIndex = i;
            }
        }
    }

    private void initializeLanguages() {
        //*test
        //generate random boolean for the downloaded flag
        Random random = new Random();

        for (int i = 0; i < concatLanguages.length; i++) {
            String[] concatLanguage = concatLanguages[i].split(Settings2.SEPARATOR);

            Language language = new Language();
            language.setLanguageCode(concatLanguage[0]);
            language.setLanguageName(concatLanguage[1]);

            if (i == currentLanguageIndex || i == englishLanguageIndex)
                language.setDownloaded(true);
            else
                language.setDownloaded(random.nextBoolean());

            languages.add(language);
        }
    }

    private void createAdapter() {


        adapter = new LanguageListAdapter(this, R.layout.languages_list_item, languages);
        adapter.setCurrentLanguageIndex(currentLanguageIndex);

        setListAdapter(adapter);
    }

    private void createAnimations() {
        fadeOutLanguage = new AlphaAnimation(1, 0);
        fadeOutLanguage.setDuration(250);
        fadeOutLanguage.setFillAfter(true);

        fadeInCurrentIndicator = new AlphaAnimation(0, 1);
        fadeInCurrentIndicator.setDuration(250);
        fadeInCurrentIndicator.setFillBefore(true);

        fadeOutCurrentIndicator = new AlphaAnimation(1, 0);
        fadeOutCurrentIndicator.setDuration(250);
        fadeOutCurrentIndicator.setStartOffset(400);
        fadeOutCurrentIndicator.setFillAfter(true);

        fadeInLanguage = new AlphaAnimation(0, 1);
        fadeInLanguage.setDuration(250);
//        fadeInLanguage.setStartOffset(500);
        fadeInLanguage.setFillBefore(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        if (position == currentLanguageIndex) {
            if (!currentLanguageAnimating)
                onCurrentLanguageClick((LanguageListAdapter.ViewHolder) view.getTag());
            return;
        }

        Language selectedLanguage = languages.get(position);

        Intent intent = new Intent();
        intent.putExtra(Settings2.LANGUAGE_CODE, selectedLanguage.getLanguageCode());
        intent.putExtra(Settings2.LANGUAGE_NAME, selectedLanguage.getLanguageName());
        intent.putExtra(Settings2.LANGUAGE_DOWNLOAD_FLAG, selectedLanguage.isDownloaded());
        setResult(RESULT_OK, intent);

        finish();
    }

    private void onCurrentLanguageClick(final LanguageListAdapter.ViewHolder holder) {
        currentLanguageAnimating = true;

        fadeInCurrentIndicator.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                holder.tvCurrentLanguageIndicator.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                    holder.tvLanguage.setVisibility(View.GONE);
//                    holder.ivDownload.setVisibility(View.GONE);

                holder.tvCurrentLanguageIndicator.startAnimation(fadeOutCurrentIndicator);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeOutCurrentIndicator.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
//                    holder.tvLanguage.setVisibility(View.VISIBLE);
//                    holder.ivDownload.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                holder.tvCurrentLanguageIndicator.setVisibility(View.GONE);
                holder.tvLanguage.startAnimation(fadeInLanguage);
                holder.ivDownload.startAnimation(fadeInLanguage);

                holder.tvCurrentLanguageIndicator.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeInLanguage.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                currentLanguageAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        holder.ivDownload.startAnimation(fadeOutLanguage);
        holder.tvLanguage.startAnimation(fadeOutLanguage);
        holder.tvCurrentLanguageIndicator.startAnimation(fadeInCurrentIndicator);
    }
}
