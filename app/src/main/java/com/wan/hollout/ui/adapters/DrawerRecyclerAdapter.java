package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.entities.drawerMenu.DrawerItemCategory;
import com.wan.hollout.entities.drawerMenu.DrawerItemPage;
import com.wan.hollout.interfaces.DrawerRecyclerInterface;
import com.wan.hollout.listeners.OnSingleClickListener;
import com.wan.hollout.ui.widgets.CircleImageView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter handling list of drawer items.
 */
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class DrawerRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM_CATEGORY = 1;
    private static final int TYPE_ITEM_PAGE = 2;

    private final DrawerRecyclerInterface drawerRecyclerInterface;
    private LayoutInflater layoutInflater;
    private Context context;
    private List<DrawerItemCategory> drawerItemCategoryList = new ArrayList<>();
    private List<DrawerItemPage> drawerItemPageList = new ArrayList<>();

    /**
     * Creates an adapter that handles a list of drawer items.
     *
     * @param context                 activity context.
     * @param drawerRecyclerInterface listener indicating events that occurred.
     */
    public DrawerRecyclerAdapter(Context context, DrawerRecyclerInterface drawerRecyclerInterface) {
        this.context = context;
        this.drawerRecyclerInterface = drawerRecyclerInterface;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layoutInflater == null)
            layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ITEM_CATEGORY) {
            View view = layoutInflater.inflate(R.layout.list_item_drawer_category, parent, false);
            return new ViewHolderItemCategory(view, drawerRecyclerInterface);
        } else if (viewType == TYPE_ITEM_PAGE) {
            View view = layoutInflater.inflate(R.layout.list_item_drawer_page, parent, false);
            return new ViewHolderItemPage(view, drawerRecyclerInterface);
        } else {
            View view = layoutInflater.inflate(R.layout.list_item_drawer_header, parent, false);
            return new ViewHolderHeader(view, drawerRecyclerInterface);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderItemCategory) {
            ViewHolderItemCategory viewHolderItemCategory = (ViewHolderItemCategory) holder;

            DrawerItemCategory drawerItemCategory = getDrawerItem(position);
            viewHolderItemCategory.bindContent(drawerItemCategory);
            viewHolderItemCategory.itemText.setText(WordUtils.capitalize(drawerItemCategory.getName()));
            if (position == 1) {
                viewHolderItemCategory.itemText.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                viewHolderItemCategory.divider.setVisibility(View.VISIBLE);
            } else {
                viewHolderItemCategory.itemText.setTextColor(ContextCompat.getColor(context, R.color.text_black));
                viewHolderItemCategory.itemText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                viewHolderItemCategory.divider.setVisibility(View.GONE);
            }
            if (drawerItemCategory.getChildren() == null || drawerItemCategory.getChildren().isEmpty()) {
                viewHolderItemCategory.subMenuIndicator.setVisibility(View.INVISIBLE);
            } else {
                viewHolderItemCategory.subMenuIndicator.setVisibility(View.VISIBLE);
            }
        } else if (holder instanceof ViewHolderItemPage) {
            ViewHolderItemPage viewHolderItemPage = (ViewHolderItemPage) holder;

            DrawerItemPage drawerItemPage = getPageItem(position);
            viewHolderItemPage.bindContent(drawerItemPage);
            viewHolderItemPage.itemText.setText(drawerItemPage.getName());
        } else if (holder instanceof ViewHolderHeader) {
            ViewHolderHeader viewHolderHeader = (ViewHolderHeader) holder;

            ParseObject user = AuthUtil.getCurrentUser();
            if (user != null) {
                viewHolderHeader.userName.setText(WordUtils.capitalize(user.getString(AppConstants.APP_USER_DISPLAY_NAME)));
                String userProfilePhotoUrl = user.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
                if (userProfilePhotoUrl != null) {
                    viewHolderHeader.signedInUserImageView.setBorderColor(Color.WHITE);
                    viewHolderHeader.signedInUserImageView.setBorderWidth(5);
                    UiUtils.loadImage((Activity) context, userProfilePhotoUrl, viewHolderHeader.signedInUserImageView);
                }
                String userCoverPhotoUrl = user.getString(AppConstants.APP_USER_COVER_PHOTO);
                if (StringUtils.isNotEmpty(userCoverPhotoUrl)) {
                    UiUtils.loadImage((Activity) context, userCoverPhotoUrl, viewHolderHeader.signedInUserCoverPhotoImageView);
                } else {
                    if (StringUtils.isNotEmpty(userProfilePhotoUrl)) {
                        UiUtils.loadImage((Activity) context, userProfilePhotoUrl, viewHolderHeader.signedInUserCoverPhotoImageView);
                    }
                }
            } else {
                viewHolderHeader.userName.setText(context.getString(R.string.not_logged_in));
                viewHolderHeader.signedInUserImageView.setImageResource(R.drawable.user);
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        // Clear the animation when the view is detached. Prevent bugs during fast scroll.
        if (holder instanceof ViewHolderItemCategory) {
            ((ViewHolderItemCategory) holder).layout.clearAnimation();
        } else if (holder instanceof ViewHolderItemPage) {
            ((ViewHolderItemPage) holder).layout.clearAnimation();
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        // Apply the animation when the view is attached
        if (holder instanceof ViewHolderItemCategory) {
            setAnimation(((ViewHolderItemCategory) holder).layout);
        } else if (holder instanceof ViewHolderItemPage) {
            setAnimation(((ViewHolderItemPage) holder).layout);
        }
    }

    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(View viewToAnimate) {
        Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        viewToAnimate.startAnimation(animation);
    }

    // This method returns the number of items present in the list
    @Override
    public int getItemCount() {
        return drawerItemCategoryList.size() + drawerItemPageList.size() + 1; // the number of items in the list will be +1 the titles including the header view.
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_HEADER;
        else if (position <= drawerItemCategoryList.size())
            return TYPE_ITEM_CATEGORY;
        else
            return TYPE_ITEM_PAGE;
    }

    private DrawerItemCategory getDrawerItem(int position) {
        return drawerItemCategoryList.get(position - 1);
    }

    private DrawerItemPage getPageItem(int position) {
        return drawerItemPageList.get(position - drawerItemCategoryList.size() - 1);
    }

    public void addDrawerItem(DrawerItemCategory drawerItemCategory) {
        drawerItemCategoryList.add(drawerItemCategory);
    }

    // Provide a reference to the views for each data item
    @SuppressWarnings("WeakerAccess")
    static class ViewHolderItemPage extends RecyclerView.ViewHolder {

        @BindView(R.id.drawer_list_item_text)
        TextView itemText;

        @BindView(R.id.drawer_list_item_layout)
        View layout;

        private DrawerItemPage drawerItemPage;

        public ViewHolderItemPage(View itemView, final DrawerRecyclerInterface drawerRecyclerInterface) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerRecyclerInterface.onPageSelected(v, drawerItemPage);
                }
            });
        }

        public void bindContent(DrawerItemPage drawerItemPage) {
            this.drawerItemPage = drawerItemPage;
        }

    }

    // Provide a reference to the views for each data item
    @SuppressWarnings("WeakerAccess")
    static class ViewHolderItemCategory extends RecyclerView.ViewHolder {
        @BindView(R.id.drawer_list_item_text)
        TextView itemText;

        @BindView(R.id.drawer_list_item_indicator)
        ImageView subMenuIndicator;

        @BindView(R.id.drawer_list_item_layout)
        LinearLayout layout;

        @BindView(R.id.drawer_list_item_divider)
        View divider;

        private DrawerItemCategory drawerItemCategory;

        ViewHolderItemCategory(View itemView, final DrawerRecyclerInterface drawerRecyclerInterface) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    drawerRecyclerInterface.onCategorySelected(v, drawerItemCategory);
                }
            });
        }

        void bindContent(DrawerItemCategory drawerItemCategory) {
            this.drawerItemCategory = drawerItemCategory;
        }
    }

    @SuppressWarnings("WeakerAccess")
    static class ViewHolderHeader extends RecyclerView.ViewHolder {

        @BindView(R.id.signed_in_user_image_view)
        CircleImageView signedInUserImageView;

        @BindView(R.id.signed_in_user_name)
        TextView userName;

        @BindView(R.id.signed_in_user_cover_image_view)
        ImageView signedInUserCoverPhotoImageView;

        public ViewHolderHeader(View headerView, final DrawerRecyclerInterface drawerRecyclerInterface) {
            super(headerView);
            ButterKnife.bind(this, headerView);
            headerView.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    drawerRecyclerInterface.onHeaderSelected();
                }
            });
        }
    }
}
