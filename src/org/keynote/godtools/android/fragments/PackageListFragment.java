package org.keynote.godtools.android.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.business.GTPackage;

import java.util.List;

public class PackageListFragment extends ListFragment {


    public interface OnPackageSelectedListener {
        public void onPackageSelected(GTPackage gtPackage);
    }

    private List<GTPackage> listPackages;
    private OnPackageSelectedListener mListener;

    public static PackageListFragment newInstance(List<GTPackage> packages){
        PackageListFragment frag = new PackageListFragment();
        frag.setPackages(packages);
        return frag;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setCacheColorHint(Color.TRANSPARENT);
        getListView().setDivider(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnPackageSelectedListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        PackageListAdapter adapter = new PackageListAdapter(getActivity(), listPackages);
        setListAdapter(adapter);
    }

    public void setPackages(List<GTPackage> packages){
        this.listPackages = packages;
    }

    public void disable(){

    }

    public void enable(){

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mListener.onPackageSelected(listPackages.get(position));
    }

    private class PackageListAdapter extends ArrayAdapter<GTPackage> {

        private List<GTPackage> listPackages;
        private Context context;
        private LayoutInflater inflater;


        public PackageListAdapter(Context context, List<GTPackage> listPackages) {
            super(context, R.layout.list_item_package, listPackages);
            this.context = context;
            this.listPackages = listPackages;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            GTPackage gtp = listPackages.get(position);

            ViewHolder holder;
            if (convertView == null) {

                convertView = inflater.inflate(R.layout.list_item_package, parent, false);

                holder = new ViewHolder();
                holder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
                holder.tvPackageName = (TextView) convertView.findViewById(R.id.tvPackageName);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // set values
            holder.ivIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.homescreen_kgp_icon_2x));
            holder.tvPackageName.setText(gtp.getCode());

            return convertView;
        }

        private class ViewHolder {
            ImageView ivIcon;
            TextView tvPackageName;
        }
    }
}
