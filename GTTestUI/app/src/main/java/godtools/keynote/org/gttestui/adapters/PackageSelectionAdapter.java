package godtools.keynote.org.gttestui.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import godtools.keynote.org.gttestui.R;

/**
 * Created by rmatt on 2/15/2017.
 */

public class PackageSelectionAdapter extends RecyclerView.Adapter<PackageSelectionAdapter.ViewHolder> {

    List<String> mLanguages;
    int progress;

    public PackageSelectionAdapter(List<String> languages) {
        this.mLanguages = languages;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_item_package_selection, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String languageName = mLanguages.get(position);
        if (position == 0) {
            holder.listItemPackageSelectionProgressBar.setProgress(progress);
        } else if (position == 1)
        {
            holder.listItemPackageSelectionProgressBar.setProgress(40);
        }
        else
        {
            holder.listItemPackageSelectionProgressBar.setProgress(80);
        }
    }

    @Override
    public long getItemId(int position) {
        return mLanguages.get(position).hashCode();
    }

    //TODO: add key  and map of keys
    public void updateProgress(int progress) {
        this.progress = progress;
        notifyItemChanged(0);

    }

    @Override
    public int getItemCount() {
        return this.mLanguages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @BindView(R.id.list_item_package_selection_progress_bar)
        ProgressBar listItemPackageSelectionProgressBar;
        @BindView(R.id.list_item_package_selection_card_view)
        CardView listItemPackageSelectionCardView;
        @BindView(R.id.list_item_package_selection_rich_media_image_view)
        ImageView listItemPackageSelectionRichMediaImageView;
        @BindView(R.id.list_item_package_selection_package_title_textview)
        TextView listItemPackageSelectionPackageTitleTextview;
        @BindView(R.id.list_item_package_selection_shares_textview)
        TextView listItemPackageSelectionSharesTextview;
        @BindView(R.id.list_item_package_selection_switch_language_imageview)
        ImageButton listItemPackageSelectionSwitchLanguageImageview;
        @BindView(R.id.list_item_package_selection_info_imageview)
        ImageButton listItemPackageSelectionInfoImageview;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            view.setSelected(!view.isSelected());

            Toast.makeText(view.getContext(), "onClick ListView", Toast.LENGTH_LONG).show();
        }
    }
}
