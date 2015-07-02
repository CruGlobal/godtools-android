package org.keynote.godtools.android.everystudent;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.utils.GoogleAnalytics;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.keynote.godtools.android.utils.Constants.EMPTY_STRING;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;

public class EveryStudentView extends Activity
{
    String title = EMPTY_STRING;
    String category = EMPTY_STRING;

    @SuppressWarnings("ResourceType")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (settings.getBoolean("wakelock", true))
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        setContentView(R.layout.everystudent_view);
        this.getWindow().setWindowAnimations(android.R.anim.slide_in_left);

        TextView content = (TextView) this.findViewById(R.id.everystudent_content);

        Uri uri = getIntent().getData();
        Cursor cur = getContentResolver().query(uri, null, null, null, null);

        if (cur.moveToFirst())
        {
            title = cur.getString(cur.getColumnIndex(EveryStudentDatabase.TITLE));
            category = cur.getString(cur.getColumnIndex(EveryStudentDatabase.CATEGORY));
            setTitle(title);
            String text = cur.getString(cur.getColumnIndex(EveryStudentDatabase.CONTENT)).replaceFirst("\n", EMPTY_STRING).trim();
            if (getIntent().getStringExtra(SearchManager.QUERY) != null)
            {
                content.setText(text, TextView.BufferType.SPANNABLE);
                Spannable str = (Spannable) content.getText();

                String query = getIntent().getStringExtra(SearchManager.QUERY).trim();
                String[] terms = query.split("[\\s]");
                List<String> termsList = Arrays.asList(terms);
                String pattern = EMPTY_STRING;

                Iterator<String> itr = termsList.iterator();
                while (itr.hasNext())
                {
                    pattern += itr.next().trim() + "[^\\s,\\.\\?:;]*";
                    if (itr.hasNext())
                    {
                        pattern += "|";
                    }
                }

                Pattern myPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                Matcher myMatcher = myPattern.matcher(text);
                while (myMatcher.find())
                {
                    str.setSpan(new BackgroundColorSpan(android.graphics.Color.YELLOW), myMatcher.start(), myMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    str.setSpan(new ForegroundColorSpan(android.graphics.Color.BLACK), myMatcher.start(), myMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            else
            {
                content.setText(text);
            }

            recordScreenView();
        }
        else
        {
            Toast.makeText(getBaseContext(), "Could not load the content.", Toast.LENGTH_LONG).show();
            this.finish();
        }

        cur.close();
    }

    private void recordScreenView()
    {
        Tracker tracker = GoogleAnalytics.getTracker(getApplicationContext());

        tracker.setScreenName("everystudent-" + massageTitleToTrainCase());

        tracker.send(new HitBuilders.AppViewBuilder()
                .setCustomDimension(1, "everystudent")
                .setCustomDimension(2, "en_classic")
                .setCustomDimension(3, "en_classic-everystudent-1") //language-package-version_number
                .build());
    }

    private String massageTitleToTrainCase()
    {
        return title.replaceAll("\\p{Punct}]", EMPTY_STRING).toLowerCase().replaceAll("\\s", "-");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.everystudent, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.search:
                onSearchRequested();
                return true;
            default:
                return false;
        }
    }
}