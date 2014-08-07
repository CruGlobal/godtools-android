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

import com.squareup.picasso.Picasso;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.io.File;
import java.util.List;

public class PackageListFragment extends ListFragment {


    public interface OnPackageSelectedListener {
        public void onPackageSelected(GTPackage gtPackage);
    }

    private List<GTPackage> listPackages;
    private PackageListAdapter mAdapter;
    private OnPackageSelectedListener mListener;

    public static PackageListFragment newInstance(List<GTPackage> packages) {
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

        mAdapter = new PackageListAdapter(getActivity(), listPackages);
        setListAdapter(mAdapter);
    }

    public void setPackages(List<GTPackage> packages) {
        this.listPackages = packages;
    }

    public void refreshList(List<GTPackage> packages) {
        mAdapter.refresh(packages);
    }

    public void disable() {
        mAdapter.disableClick();
    }

    public void enable() {
        mAdapter.enableClick();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mListener.onPackageSelected(listPackages.get(position));
    }

    private class PackageListAdapter extends ArrayAdapter<GTPackage> {

        private List<GTPackage> listPackages;
        private LayoutInflater inflater;
        private boolean mIsEnabled;
        private String resourcesDir;

        public PackageListAdapter(Context context, List<GTPackage> listPackages) {
            super(context, R.layout.list_item_package, listPackages);
            this.listPackages = listPackages;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mIsEnabled = true;

            SnuffyApplication mApp = (SnuffyApplication) getActivity().getApplication();
            resourcesDir = mApp.getDocumentsDir().getAbsolutePath() + "/resources/";

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
                holder.vGray = convertView.findViewById(R.id.vGray);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (mIsEnabled)
                holder.vGray.setVisibility(View.GONE);
            else
                holder.vGray.setVisibility(View.VISIBLE);

            String name = gtp.getName();
            if (gtp.getStatus().equalsIgnoreCase("draft"))
                name = String.format("%s (v%s)", name, gtp.getVersion());

            // set values
            holder.tvPackageName.setText(name);

            Picasso.with(getActivity())
                    .load(new File(resourcesDir + gtp.getIcon()))
                    .into(holder.ivIcon);

            return convertView;
        }

        private class ViewHolder {
            ImageView ivIcon;
            TextView tvPackageName;
            View vGray;
        }

        public void refresh(List<GTPackage> packageList) {
            this.listPackages.clear();
            this.listPackages.addAll(packageList);
            notifyDataSetChanged();
        }

        @Override
        public boolean isEnabled(int position) {
            return mIsEnabled;
        }

        public void enableClick() {
            mIsEnabled = true;
            this.notifyDataSetChanged();
        }

        public void disableClick() {
            mIsEnabled = false;
            this.notifyDataSetChanged();
        }
    }
}
