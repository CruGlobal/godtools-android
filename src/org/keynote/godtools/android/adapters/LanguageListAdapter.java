package org.keynote.godtools.android.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.business.Language;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john.jarder on 6/16/14.
 */
public class LanguageListAdapter extends ArrayAdapter<Language> {
    static final String debugTag = "LanguageListAdapter";

    ArrayList<Language> languageList;
    Context context;

    int currentLanguageIndex;

    int currentLanguageColor = R.color.settings_listitem_item;
    int otherLanguageColor = R.color.gray_60;

    public LanguageListAdapter(Context context, int resource, List<Language> items) {
        super(context, resource, items);
        this.context = context;
        languageList = new ArrayList<Language>(items);

        Log.d(debugTag, "languagesList count: " + languageList.size());

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.languages_list_item, parent, false);

            holder = new ViewHolder();
            holder.tvLanguage = (TextView) convertView.findViewById(R.id.tvLanguageName);
            holder.ivDownload = (ImageView) convertView.findViewById(R.id.ivDownloadLanguage);
            holder.tvCurrentLanguageIndicator = (TextView) convertView.findViewById(R.id.tvCurrentLanguageIndicator);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Log.d(debugTag, "getVIew: current language index: " + currentLanguageIndex);

        Language lang = languageList.get(position);
        //if language has been downloaded, don't show the download icon
        int visibility = lang.isDownloaded() ? View.GONE : View.VISIBLE;

        holder.tvLanguage.setText(lang.getLanguageName());
        holder.ivDownload.setVisibility(visibility);

        if (position == currentLanguageIndex) {
            holder.tvLanguage.setTextColor(context.getResources().getColor(currentLanguageColor));
            holder.tvLanguage.setTypeface(holder.tvLanguage.getTypeface(), Typeface.BOLD);
        } else {
            holder.tvLanguage.setTextColor(context.getResources().getColor(otherLanguageColor));
            holder.tvLanguage.setTypeface(holder.tvLanguage.getTypeface(), Typeface.NORMAL);
        }


        return convertView;
    }

    public static class ViewHolder {
        public TextView tvLanguage;
        public ImageView ivDownload;
        public TextView tvCurrentLanguageIndicator;
    }

    public void setCurrentLanguageIndex(int index) {
        Log.d(debugTag, "current language index: " + index);
        currentLanguageIndex = index;
    }
}
