package org.keynote.godtools.android.customviews;

import android.app.Activity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.utils.MenuParser;

import java.util.ArrayList;

/**
 * TODO: document your custom view class.
 */
public class CustomActionBar {
    static final String LOGTAG = "CustomActionBar";
    Activity activity;

    //    View contTitle;
    ImageView ivBack;
    LinearLayout contMenu;
    TextView tvTitle;
    ArrayList<MenuItem> menuItems;

    public CustomActionBar(Activity activity, View actionLayout) {
        this.activity = activity;

        ivBack = (ImageView) actionLayout.findViewById(R.id.ivBack);
        contMenu = (LinearLayout) actionLayout.findViewById(R.id.contMenu);
        tvTitle = (TextView) actionLayout.findViewById(R.id.tvPageTitle);

    }

    public void setPageTitle(String title) {
        tvTitle.setText(title);
    }

    public void setUpMenu(ArrayList<MenuParser.ActionMenu> actionMenuList, ArrayList<MenuItem> menuItems) {
        this.menuItems = menuItems;

        for (MenuParser.ActionMenu actionMenu : actionMenuList) {
            addMenuToActionbar(actionMenu);
        }
    }

    protected void addMenuToActionbar(final MenuParser.ActionMenu actionMenu) {
        actionMenu.menuItemView = (ImageView) activity.getLayoutInflater().inflate(R.layout.actionmenu_imageview, null);
        actionMenu.menuItemView.setImageResource(actionMenu.itemIconResId);

        actionMenu.menuItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuItem item = menuItems.get(actionMenu.itemIndex);
                activity.onOptionsItemSelected(item);
            }
        });

        contMenu.addView(actionMenu.menuItemView);
    }

    public void applyBackButtonState(boolean enabled) {
        if (enabled) {
            ivBack.setVisibility(View.VISIBLE);
            ivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(LOGTAG, "backPress OnclickListener");
                    activity.onBackPressed();
                }
            });
        } else {
            ivBack.setVisibility(View.GONE);
        }
    }

}
