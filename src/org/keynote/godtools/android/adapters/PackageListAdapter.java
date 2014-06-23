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
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.Language;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john.jarder on 6/16/14.
 */
public class PackageListAdapter extends ArrayAdapter<GTPackage> {
    static final String LOGTAG = "PackageListAdapter";

    Context context;
    List<GTPackage> packages;

    int currentLanguageIndex;

    public PackageListAdapter(Context context, int resource, List<GTPackage> items) {
        super(context, resource, items);
        this.context = context;
        packages = items;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.packages_list_item, parent, false);

            holder = new ViewHolder();
            holder.tvPackageName = (TextView) convertView.findViewById(R.id.tvPackageName);
            holder.ivPackageImage = (ImageView) convertView.findViewById(R.id.ivPackageImage);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvPackageName.setText(packages.get(position).getTitle());
        holder.ivPackageImage.setImageResource(packages.get(position).getImage());

        return convertView;
    }

    private static class ViewHolder {
        public TextView tvPackageName;
        public ImageView ivPackageImage;
    }
}
