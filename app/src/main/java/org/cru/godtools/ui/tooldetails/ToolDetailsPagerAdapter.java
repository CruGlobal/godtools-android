package org.cru.godtools.ui.tooldetails;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cru.godtools.R;
import org.cru.godtools.databinding.ToolDetailsPageDescriptionBinding;
import org.cru.godtools.databinding.ToolDetailsPageLanguagesBinding;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.library.baseAdapters.BR;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager.widget.PagerAdapter;

class ToolDetailsPagerAdapter extends PagerAdapter {
     final Context mContext;
     final LifecycleOwner mLifecycleOwner;
     final ToolDetailsFragmentDataModel mDataModel;

     ToolDetailsPagerAdapter(final Context context, final LifecycleOwner lifecycleOwner,
                             final ToolDetailsFragmentDataModel dataModel) {
         mContext = context;
         mLifecycleOwner = lifecycleOwner;
         mDataModel = dataModel;
     }

     @NonNull
     @Override
     public Object instantiateItem(@NonNull final ViewGroup container, int position) {
         final ViewDataBinding binding;
         switch (position) {
             case 0:
                 binding = ToolDetailsPageDescriptionBinding
                         .inflate(LayoutInflater.from(container.getContext()), container, true);
                 binding.setVariable(BR.tool, mDataModel.getTool());
                 binding.setVariable(BR.translation, mDataModel.getPrimaryTranslation());
                 break;
             case 1:
                 binding = ToolDetailsPageLanguagesBinding
                         .inflate(LayoutInflater.from(container.getContext()), container, true);
                 binding.setVariable(BR.languages, mDataModel.getAvailableLanguages());
                 break;
             default:
                 throw new IllegalArgumentException("page " + position + " is not a valid page");
         }
         binding.setLifecycleOwner(mLifecycleOwner);
         return binding;
     }

     @Override
     public void destroyItem(@NonNull ViewGroup container, int position,
                             @NonNull Object object) {
         container.removeView(((ViewDataBinding) object).getRoot());
     }

     @Override
     public int getCount() {
         return 2;
     }

     @Nullable
     @Override
     public CharSequence getPageTitle(final int position) {
         switch (position) {
             case 0:
                 return mContext.getString(R.string.label_tools_about);
             case 1:
                 final List<Locale> languages = mDataModel.getAvailableLanguages().getValue();
                 final int count = languages != null ? languages.size() : 0;
                 return mContext.getResources().getQuantityString(R.plurals.label_tools_languages, count, count);
         }
         return "";
     }

     @Override
     public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
         return DataBindingUtil.findBinding(view) == object;
     }
 }
