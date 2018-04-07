package com.wan.hollout.ui.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wan.hollout.R;
import com.wan.hollout.interfaces.ButterBarOnClickListener;
import com.wan.hollout.interfaces.ButterBarOnLongClickListener;
import com.wan.hollout.ui.widgets.validation.BadgeHelper;
import com.wan.hollout.ui.widgets.validation.BadgeItem;
import com.wan.hollout.ui.widgets.validation.ButterBarItem;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HolloutButterBar extends RelativeLayout {

    private static final String TAG = "HolloutButterBar";

    private static final String CURRENT_SELECTED_ITEM_BUNDLE_KEY = "currentItem";

    private static final String BUDGES_ITEM_BUNDLE_KEY = "budgeItem";

    private static final String CHANGED_ICON_AND_TEXT_BUNDLE_KEY = "changedIconAndText";

    private static final String CENTRE_BUTTON_ICON_KEY = "centreButtonIconKey";

    private static final String CENTRE_BUTTON_COLOR_KEY = "centreButtonColorKey";

    private static final String BUTTER_BACKGROUND_COLOR_KEY = "backgroundColorKey";

    private static final String BADGE_FULL_TEXT_KEY = "badgeFullTextKey";

    private static final int NOT_DEFINED = -777; //random number, not - 1 because it is Color.WHITE

    private static final int MAX_BUTTER_BAR_ITEM_SIZE = 4;

    private static final int MIN_BUTTER_BAR_ITEM_SIZE = 2;

    private List<ButterBarItem> butterBarItems = new ArrayList<>();

    private List<View> butterBarItemList = new ArrayList<>();

    private List<RelativeLayout> badgeList = new ArrayList<>();

    private HashMap<Integer, Object> badgeSaveInstanceHashMap = new HashMap<>();

    private HashMap<Integer, ButterBarItem> changedItemAndIconHashMap = new HashMap<>();

    private ButterBarOnClickListener butterBarOnClickListener;

    private ButterBarOnLongClickListener butterBarOnLongClickListener;

    private Bundle savedInstanceState;

    private FloatingActionButton fab;

    private RelativeLayout centreBackgroundView;

    private LinearLayout leftContent, rightContent;

    private BezierView centreContent;

    private Typeface customFont;

    private Context context;

    private final int butterBarNavigationHeight = (int) getResources().getDimension(R.dimen.butter_bar_navigation_height);

    private final int mainContentHeight = (int) getResources().getDimension(R.dimen.main_content_height);

    private final int centreContentWight = (int) getResources().getDimension(R.dimen.centre_content_width);

    private final int centreButtonSize = (int) getResources().getDimension(R.dimen.butter_bar_centre_button_default_size);

    private int butterBarItemIconSize = NOT_DEFINED;

    private int butterBarItemIconOnlySize = NOT_DEFINED;

    private int butterBarItemTextSize = NOT_DEFINED;

    private int butterBarBackgroundColor = NOT_DEFINED;
    private boolean fabVisible = true;

    private int centreButtonColor = NOT_DEFINED;

    private int centreButtonIconColor = NOT_DEFINED;

    private int centreButtonIcon = NOT_DEFINED;

    private int activeButterBarItemColor = NOT_DEFINED;

    private int inActiveButterBarItemColor = NOT_DEFINED;

    private int centreButtonRippleColor = NOT_DEFINED;

    private int currentSelectedItem = -1;

    private int contentWidth;

    private boolean isTextOnlyMode = false;

    private boolean isIconOnlyMode = false;

    private boolean isCustomFont = false;

    private boolean isCentreButtonIconColorFilterEnabled = true;

    private boolean shouldShowBadgeWithNinePlus = true;

    /**
     * Constructors
     */
    public HolloutButterBar(Context context) {
        this(context, null);
    }

    public HolloutButterBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HolloutButterBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
    }

    /**
     * Init custom attributes
     *
     * @param attrs attributes
     */
    private void init(AttributeSet attrs) {
        if (attrs != null) {
            Resources resources = getResources();
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HolloutButterBar);
            butterBarItemIconSize = typedArray.getDimensionPixelSize(R.styleable.HolloutButterBar_butter_bar_item_icon_size, resources.getDimensionPixelSize(R.dimen.butter_bar_item_icon_default_size));
            butterBarItemIconOnlySize = typedArray.getDimensionPixelSize(R.styleable.HolloutButterBar_butter_bar_item_icon_only_size, resources.getDimensionPixelSize(R.dimen.butter_bar_item_icon_only_size));
            butterBarItemTextSize = typedArray.getDimensionPixelSize(R.styleable.HolloutButterBar_butter_bar_item_text_size, resources.getDimensionPixelSize(R.dimen.butter_bar_item_text_default_size));
            butterBarItemIconOnlySize = typedArray.getDimensionPixelSize(R.styleable.HolloutButterBar_butter_bar_item_icon_only_size, resources.getDimensionPixelSize(R.dimen.butter_bar_item_icon_only_size));
            butterBarBackgroundColor = typedArray.getColor(R.styleable.HolloutButterBar_butter_bar_background_color, resources.getColor(R.color.colorPrimary));
            centreButtonColor = typedArray.getColor(R.styleable.HolloutButterBar_centre_button_color, resources.getColor(R.color.centre_button_color));
            activeButterBarItemColor = typedArray.getColor(R.styleable.HolloutButterBar_active_item_color, resources.getColor(R.color.whitesmoke));
            inActiveButterBarItemColor = typedArray.getColor(R.styleable.HolloutButterBar_inactive_item_color, resources.getColor(R.color.default_inactive_item_color));
            centreButtonIcon = typedArray.getResourceId(R.styleable.HolloutButterBar_centre_button_icon, R.drawable.near_me);
            centreButtonIconColor = typedArray.getColor(R.styleable.HolloutButterBar_centre_button_icon_color, resources.getColor(R.color.whitesmoke));
            fabVisible = typedArray.getBoolean(R.styleable.HolloutButterBar_centre_button_visible, true);
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /**
         * Set default colors and sizes
         */
        if (butterBarBackgroundColor == NOT_DEFINED)
            butterBarBackgroundColor = ContextCompat.getColor(context, R.color.colorPrimary);

        if (centreButtonColor == NOT_DEFINED)
            centreButtonColor = ContextCompat.getColor(context, R.color.centre_button_color);

        if (centreButtonIcon == NOT_DEFINED)
            centreButtonIcon = R.drawable.ic_format_list_bulleted_black_48dp;

        if (activeButterBarItemColor == NOT_DEFINED)
            activeButterBarItemColor = ContextCompat.getColor(context, R.color.whitesmoke);

        if (inActiveButterBarItemColor == NOT_DEFINED)
            inActiveButterBarItemColor = ContextCompat.getColor(context, R.color.default_inactive_item_color);

        if (butterBarItemTextSize == NOT_DEFINED)
            butterBarItemTextSize = (int) getResources().getDimension(R.dimen.butter_bar_item_text_default_size);

        if (butterBarItemIconSize == NOT_DEFINED)
            butterBarItemIconSize = (int) getResources().getDimension(R.dimen.butter_bar_item_icon_default_size);

        if (butterBarItemIconOnlySize == NOT_DEFINED)
            butterBarItemIconOnlySize = (int) getResources().getDimension(R.dimen.butter_bar_item_icon_only_size);

        if (centreButtonRippleColor == NOT_DEFINED)
            centreButtonRippleColor = ContextCompat.getColor(context, R.color.colorBackgroundHighlightWhite);

        if (centreButtonIconColor == NOT_DEFINED)
            centreButtonIconColor = ContextCompat.getColor(context, R.color.whitesmoke);

        /**
         * Set main layout size and color
         */
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = butterBarNavigationHeight;
        setBackgroundColor(ContextCompat.getColor(context, R.color.butter_bar_transparent));
        setLayoutParams(params);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        /**
         * Restore current item index from savedInstance
         */
        restoreCurrentItem();

        /**
         * Trow exceptions if items size is greater than 4 or lesser than 2
         */
        if (butterBarItems.size() < MIN_BUTTER_BAR_ITEM_SIZE && !isInEditMode()) {
            throw new NullPointerException("Your Butter bar item count must be greater than 1 ," +
                    " your current items count isa : " + butterBarItems.size());
        }

        if (butterBarItems.size() > MAX_BUTTER_BAR_ITEM_SIZE && !isInEditMode()) {
            throw new IndexOutOfBoundsException("Your items count maximum can be 4," +
                    " your current items count is : " + butterBarItems.size());
        }

        /**
         * Get left or right content width
         */
        contentWidth = (width - butterBarNavigationHeight) / 2;

        /**
         * Removing all view for not being duplicated
         */
        removeAllViews();

        /**
         * Views initializations and customizing
         */
        initAndAddViewsToMainView();

        /**
         * Redraw main view to make subviews visible
         */
        postRequestLayout();
    }

    //private methods

    /**
     * Views initializations and customizing
     */
    private void initAndAddViewsToMainView() {

        RelativeLayout mainContent = new RelativeLayout(context);
        centreBackgroundView = new RelativeLayout(context);

        leftContent = new LinearLayout(context);
        rightContent = new LinearLayout(context);

        centreContent = buildBezierView();

        fab = new FloatingActionButton(context);
        fab.setUseCompatPadding(false);
        fab.setRippleColor(centreButtonRippleColor);
        fab.setBackgroundTintList(ColorStateList.valueOf(centreButtonColor));
        fab.setImageResource(centreButtonIcon);

        if (isCentreButtonIconColorFilterEnabled)
            fab.getDrawable().setColorFilter(centreButtonIconColor, PorterDuff.Mode.SRC_IN);

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (butterBarOnClickListener != null)
                    butterBarOnClickListener.onCentreButtonClick();
            }
        });

        fab.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (butterBarOnLongClickListener != null)
                    butterBarOnLongClickListener.onCentreButtonLongClick();
                return true;
            }
        });

        UiUtils.showView(fab, fabVisible);

        /**
         * Set fab layout params
         */
        LayoutParams fabParams = new LayoutParams(centreButtonSize, centreButtonSize);
        fabParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        /**
         * Main content size
         */
        LayoutParams mainContentParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mainContentHeight);
        mainContentParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        /**
         * Centre content size
         */
        LayoutParams centreContentParams = new LayoutParams(centreContentWight, butterBarNavigationHeight);
        centreContentParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        centreContentParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        /**
         * Centre Background View content size and position
         */
        LayoutParams centreBackgroundViewParams = new LayoutParams(centreContentWight, mainContentHeight);
        centreBackgroundViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        centreBackgroundViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        /**
         * Left content size
         */
        LayoutParams leftContentParams = new LayoutParams(contentWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        leftContentParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        leftContentParams.addRule(LinearLayout.HORIZONTAL);

        /**
         * Right content size
         */
        LayoutParams rightContentParams = new LayoutParams(contentWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        rightContentParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rightContentParams.addRule(LinearLayout.HORIZONTAL);

        /**
         * Adding views background colors
         */
        setBackgroundColors();

        /**
         * Adding view to centreContent
         */
        centreContent.addView(fab, fabParams);

        /**
         * Adding views to mainContent
         */
        mainContent.addView(leftContent, leftContentParams);
        mainContent.addView(rightContent, rightContentParams);

        /**
         * Adding views to mainView
         */
        addView(centreBackgroundView, centreBackgroundViewParams);
        addView(centreContent, centreContentParams);
        addView(mainContent, mainContentParams);

        /**
         * Restore changed icons and texts from savedInstance
         */
        restoreChangedIconsAndTexts();

        /**
         * Adding current Butter Bar items to left and right content
         */
        addButterBarItems(leftContent, rightContent);
    }

    public FloatingActionButton getFab() {
        return fab;
    }

    /**
     * Adding given Butter bar items to content
     *
     * @param leftContent  to left content
     * @param rightContent and right content
     */
    private void addButterBarItems(LinearLayout leftContent, LinearLayout rightContent) {

        /**
         * Removing all views for not being duplicated
         */
        if (leftContent.getChildCount() > 0 || rightContent.getChildCount() > 0) {
            leftContent.removeAllViews();
            rightContent.removeAllViews();
        }

        /**
         * Clear butterBarItemList and badgeList for not being duplicated
         */
        butterBarItemList.clear();
        badgeList.clear();

        /**
         * Getting LayoutInflater to inflate Butter bar  item view from XML
         */
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < butterBarItems.size(); i++) {

            final int index = i;
            int targetWidth;

            if (butterBarItems.size() > MIN_BUTTER_BAR_ITEM_SIZE) {
                targetWidth = contentWidth / 2;
            } else {
                targetWidth = contentWidth;
            }

            LayoutParams textAndIconContainerParams = new LayoutParams(
                    targetWidth, mainContentHeight);
            RelativeLayout textAndIconContainer = (RelativeLayout) inflater.inflate(R.layout.butter_bar_item_view, this, false);
            textAndIconContainer.setLayoutParams(textAndIconContainerParams);

            ImageView ButterBarItemIcon = (ImageView) textAndIconContainer.findViewById(R.id.butter_bar_icon);
            TextView ButterBarItemText = (TextView) textAndIconContainer.findViewById(R.id.butter_bar_text);
            RelativeLayout badgeContainer = (RelativeLayout) textAndIconContainer.findViewById(R.id.badge_container);
            ButterBarItemIcon.setImageResource(butterBarItems.get(i).getItemIcon());
            ButterBarItemText.setText(butterBarItems.get(i).getItemName());
            ButterBarItemText.setTextSize(TypedValue.COMPLEX_UNIT_PX, butterBarItemTextSize);

            /**
             * Set custom font to Butter bar item textView
             */
            if (isCustomFont)
                ButterBarItemText.setTypeface(customFont);

            /**`
             * Hide item icon and show only text
             */
            if (isTextOnlyMode)
                UiUtils.showView(ButterBarItemIcon, false);

            /**
             * Hide item text and change icon size
             */
            ViewGroup.LayoutParams iconParams = ButterBarItemIcon.getLayoutParams();
            if (isIconOnlyMode) {
                iconParams.height = butterBarItemIconOnlySize;
                iconParams.width = butterBarItemIconOnlySize;
                ButterBarItemIcon.setLayoutParams(iconParams);
                UiUtils.showView(ButterBarItemText, false);
            } else {
                iconParams.height = butterBarItemIconSize;
                iconParams.width = butterBarItemIconSize;
                ButterBarItemIcon.setLayoutParams(iconParams);
            }

            /**
             * Adding Butter Bar items to item list for future
             */
            butterBarItemList.add(textAndIconContainer);

            /**
             * Adding badge items to badge list for future
             */
            badgeList.add(badgeContainer);

            /**
             * Adding sub views to left and right sides
             */
            if (butterBarItems.size() == MIN_BUTTER_BAR_ITEM_SIZE && leftContent.getChildCount() == 1) {
                rightContent.addView(textAndIconContainer, textAndIconContainerParams);
            } else if (butterBarItems.size() > MIN_BUTTER_BAR_ITEM_SIZE && leftContent.getChildCount() == 2) {
                rightContent.addView(textAndIconContainer, textAndIconContainerParams);
            } else {
                leftContent.addView(textAndIconContainer, textAndIconContainerParams);
            }

            /**
             * Changing current selected item tint
             */
            if (i == currentSelectedItem) {
                ButterBarItemText.setTextColor(activeButterBarItemColor);
                UiUtils.tintImageView(ButterBarItemIcon, activeButterBarItemColor);
            } else {
                ButterBarItemText.setTextColor(inActiveButterBarItemColor);
                UiUtils.tintImageView(ButterBarItemIcon, inActiveButterBarItemColor);
            }

            textAndIconContainer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateButterBarItems(index);
                }
            });

            textAndIconContainer.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (butterBarOnLongClickListener != null)
                        butterBarOnLongClickListener.onItemLongClick(index, butterBarItems.get(index).getItemName());
                    return true;
                }
            });
        }
        /**
         * Restore available badges from saveInstance
         */
        restoreBadges();
    }

    public void inactivateAll() {

        /**
         * Change active and inactive icon and text color
         */
        for (int i = 0; i < butterBarItemList.size(); i++) {
            RelativeLayout textAndIconContainer = (RelativeLayout) butterBarItemList.get(i);
            ImageView butterBarItemIcon = (ImageView) textAndIconContainer.findViewById(R.id.butter_bar_icon);
            TextView butterBarItemText = (TextView) textAndIconContainer.findViewById(R.id.butter_bar_text);
            butterBarItemText.setTextColor(activeButterBarItemColor);
            UiUtils.tintImageView(butterBarItemIcon, inActiveButterBarItemColor);
        }

    }

    /**
     * Update selected item and change it's and non selected item tint
     *
     * @param selectedIndex item in index
     */
    public void updateButterBarItems(final int selectedIndex) {
        HolloutLogger.d(TAG, "SelectedItemIndex = " + selectedIndex);
        /**
         * return if item already selected
         */
        if (selectedIndex != -1) {
            if (currentSelectedItem == selectedIndex) {
                if (butterBarOnClickListener != null)
                    butterBarOnClickListener.onItemReselected(selectedIndex, butterBarItems.get(selectedIndex).getItemName());
                tintViews(selectedIndex);
                return;
            }
        } else {
            currentSelectedItem = 0;
        }

        /**
         * Change active and inactive icon and text color
         */
        if (selectedIndex != -1) {
            tintViews(selectedIndex);
        } else {
            for (int i = 0; i < butterBarItemList.size(); i++) {
                RelativeLayout textAndIconContainer = (RelativeLayout) butterBarItemList.get(i);
                ImageView butterBarItemIcon = (ImageView) textAndIconContainer.findViewById(R.id.butter_bar_icon);
                TextView butterBarItemText = (TextView) textAndIconContainer.findViewById(R.id.butter_bar_text);
                butterBarItemText.setTextColor(inActiveButterBarItemColor);
                UiUtils.tintImageView(butterBarItemIcon, inActiveButterBarItemColor);
            }
            currentSelectedItem = 0;
        }

        /**
         * Set a listener that gets fired when the selected item changes
         *
         * @param listener a listener for monitoring changes in item selection
         */
        if (selectedIndex != -1) {
            if (butterBarOnClickListener != null)
                butterBarOnClickListener.onItemClick(selectedIndex, butterBarItems.get(selectedIndex).getItemName());

            /**
             * Change current selected item index
             */
            currentSelectedItem = selectedIndex;
        } else {
            currentSelectedItem = 0;
        }

    }

    private void tintViews(int selectedIndex) {
        for (int i = 0; i < butterBarItemList.size(); i++) {
            if (i == selectedIndex) {
                RelativeLayout textAndIconContainer = (RelativeLayout) butterBarItemList.get(selectedIndex);
                ImageView butterBarItemIcon = (ImageView) textAndIconContainer.findViewById(R.id.butter_bar_icon);
                TextView butterBarItemText = (TextView) textAndIconContainer.findViewById(R.id.butter_bar_text);
                butterBarItemText.setTextColor(activeButterBarItemColor);
                UiUtils.tintImageView(butterBarItemIcon, activeButterBarItemColor);
            } else if (i == currentSelectedItem) {
                RelativeLayout textAndIconContainer = (RelativeLayout) butterBarItemList.get(i);
                ImageView butterBarItemIcon = (ImageView) textAndIconContainer.findViewById(R.id.butter_bar_icon);
                TextView butterBarItemText = (TextView) textAndIconContainer.findViewById(R.id.butter_bar_text);
                butterBarItemText.setTextColor(inActiveButterBarItemColor);
                UiUtils.tintImageView(butterBarItemIcon, inActiveButterBarItemColor);
            }
        }
    }


    /**
     * Set views background colors
     */
    private void setBackgroundColors() {
        rightContent.setBackgroundColor(butterBarBackgroundColor);
        centreBackgroundView.setBackgroundColor(butterBarBackgroundColor);
        leftContent.setBackgroundColor(butterBarBackgroundColor);
    }

    /**
     * Indicate event queue that we have changed the View hierarchy during a layout pass
     */
    private void postRequestLayout() {
        HolloutButterBar.this.getHandler().post(new Runnable() {
            @Override
            public void run() {
                HolloutButterBar.this.requestLayout();
            }
        });
    }

    /**
     * Restore current item index from savedInstance
     */
    private void restoreCurrentItem() {
        Bundle restoredBundle = savedInstanceState;
        if (restoredBundle != null) {
            if (restoredBundle.containsKey(CURRENT_SELECTED_ITEM_BUNDLE_KEY))
                currentSelectedItem = restoredBundle.getInt(CURRENT_SELECTED_ITEM_BUNDLE_KEY, 0);
        }
    }

    /**
     * Restore available badges from saveInstance
     */
    @SuppressWarnings("unchecked")
    private void restoreBadges() {
        Bundle restoredBundle = savedInstanceState;
        if (restoredBundle != null) {
            if (restoredBundle.containsKey(BADGE_FULL_TEXT_KEY)) {
                shouldShowBadgeWithNinePlus = restoredBundle.getBoolean(BADGE_FULL_TEXT_KEY);
            }
            if (restoredBundle.containsKey(BUDGES_ITEM_BUNDLE_KEY)) {
                badgeSaveInstanceHashMap = (HashMap<Integer, Object>) savedInstanceState.getSerializable(BUDGES_ITEM_BUNDLE_KEY);
                if (badgeSaveInstanceHashMap != null) {
                    for (Integer integer : badgeSaveInstanceHashMap.keySet()) {
                        BadgeHelper.forceShowBadge(
                                badgeList.get(integer),
                                (BadgeItem) badgeSaveInstanceHashMap.get(integer),
                                shouldShowBadgeWithNinePlus);
                    }
                }
            }
        }
    }

    /**
     * Restore changed icons,colors and texts from saveInstance
     */
    @SuppressWarnings("unchecked")
    private void restoreChangedIconsAndTexts() {
        Bundle restoredBundle = savedInstanceState;
        if (restoredBundle != null) {
            if (restoredBundle.containsKey(CHANGED_ICON_AND_TEXT_BUNDLE_KEY)) {
                changedItemAndIconHashMap = (HashMap<Integer, ButterBarItem>) restoredBundle.getSerializable(CHANGED_ICON_AND_TEXT_BUNDLE_KEY);
                if (changedItemAndIconHashMap != null) {
                    ButterBarItem butterBarItem;
                    for (int i = 0; i < changedItemAndIconHashMap.size(); i++) {
                        butterBarItem = changedItemAndIconHashMap.get(i);
                        butterBarItems.get(i).setItemIcon(butterBarItem.getItemIcon());
                        butterBarItems.get(i).setItemName(butterBarItem.getItemName());
                    }
                }
            }

            if (restoredBundle.containsKey(CENTRE_BUTTON_ICON_KEY)) {
                centreButtonIcon = restoredBundle.getInt(CENTRE_BUTTON_ICON_KEY);
                fab.setImageResource(centreButtonIcon);
            }

            if (restoredBundle.containsKey(BUTTER_BACKGROUND_COLOR_KEY)) {
                int backgroundColor = restoredBundle.getInt(BUTTER_BACKGROUND_COLOR_KEY);
                changeButterBarBackgroundColor(backgroundColor);
            }
        }
    }

    /**
     * Creating bezier view with params
     *
     * @return created bezier view
     */
    private BezierView buildBezierView() {
        BezierView bezierView = new BezierView(context, butterBarBackgroundColor);
        bezierView.build(centreContentWight, butterBarNavigationHeight - mainContentHeight);
        return bezierView;
    }

    /**
     * Throw Array Index Out Of Bounds Exception
     *
     * @param itemIndex item index to show on logs
     */
    private void throwArrayIndexOutOfBoundsException(int itemIndex) {
        throw new ArrayIndexOutOfBoundsException("Your item index can't be 0 or greater than butter bar item size," +
                " your items size is " + butterBarItems.size() + ", your current index is :" + itemIndex);
    }

    //public methods

    /**
     * Initialization with savedInstanceState to save current selected
     * position and current budges
     *
     * @param savedInstanceState bundle to saveInstance
     */
    public void initWithSaveInstanceState(Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
    }

    /**
     * Save budges and current position
     *
     * @param outState bundle to saveInstance
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_SELECTED_ITEM_BUNDLE_KEY, currentSelectedItem);
        outState.putInt(CENTRE_BUTTON_ICON_KEY, centreButtonIcon);
        outState.putInt(BUTTER_BACKGROUND_COLOR_KEY, butterBarBackgroundColor);
        outState.putBoolean(BADGE_FULL_TEXT_KEY, shouldShowBadgeWithNinePlus);

        if (badgeSaveInstanceHashMap.size() > 0)
            outState.putSerializable(BUDGES_ITEM_BUNDLE_KEY, badgeSaveInstanceHashMap);
        if (changedItemAndIconHashMap.size() > 0)
            outState.putSerializable(CHANGED_ICON_AND_TEXT_BUNDLE_KEY, changedItemAndIconHashMap);
    }

    /**
     * Set centre circle button background color
     *
     * @param centreButtonColor target color
     */
    public void setCentreButtonColor(@ColorInt int centreButtonColor) {
        this.centreButtonColor = centreButtonColor;
    }

    public int getCentreButtonColor() {
        return centreButtonColor;
    }

    /**
     * Set main background color
     *
     * @param butterBarBackgroundColor target color
     */
    public void setButterBarBackgroundColor(@ColorInt int butterBarBackgroundColor) {
        this.butterBarBackgroundColor = butterBarBackgroundColor;
    }

    /**
     * Set centre button icon
     *
     * @param centreButtonIcon target icon
     */
    public void setCentreButtonIcon(int centreButtonIcon) {
        this.centreButtonIcon = centreButtonIcon;
    }

    /**
     * Set active item text color
     *
     * @param activeButterBarItemColor color to change
     */
    public void setActiveButterItemColor(@ColorInt int activeButterBarItemColor) {
        this.activeButterBarItemColor = activeButterBarItemColor;
    }

    /**
     * Set inactive item text color
     *
     * @param inActiveButterBarItemColor color to change
     */
    public void setInActiveButterItemColor(@ColorInt int inActiveButterBarItemColor) {
        this.inActiveButterBarItemColor = inActiveButterBarItemColor;
    }

    /**
     * Set item icon size
     *
     * @param butterBarItemIconSize target size
     */
    public void setButterBarItemIconSize(int butterBarItemIconSize) {
        this.butterBarItemIconSize = butterBarItemIconSize;
    }

    /**
     * Set item icon size if showIconOnly() called
     *
     * @param butterBarItemIconOnlySize target size
     */
    public void setButterBarItemIconSizeInOnlyIconMode(int butterBarItemIconOnlySize) {
        this.butterBarItemIconOnlySize = butterBarItemIconOnlySize;
    }

    /**
     * Set item text size
     *
     * @param butterBarItemTextSize target size
     */
    public void setButterBarItemTextSize(int butterBarItemTextSize) {
        this.butterBarItemTextSize = butterBarItemTextSize;
    }

    /**
     * Set centre button pressed state color
     *
     * @param centreButtonRippleColor Target color
     */
    public void setCentreButtonRippleColor(int centreButtonRippleColor) {
        this.centreButtonRippleColor = centreButtonRippleColor;
    }

    /**
     * Show only text in item
     */
    public void showTextOnly() {
        isTextOnlyMode = true;
    }

    /**
     * Show only icon in item
     */
    public void showIconOnly() {
        isIconOnlyMode = true;
    }

    /**
     * Add Butter Bar item to navigation
     *
     * @param butterBarItem item to add
     */
    public void addButterBarItem(ButterBarItem butterBarItem) {
        butterBarItems.add(butterBarItem);
    }

    /**
     * Set Butter Bar item and centre click
     *
     * @param butterBarOnClickListener Butter bar click listener
     */
    public void setButterBarOnClickListener(ButterBarOnClickListener butterBarOnClickListener) {
        this.butterBarOnClickListener = butterBarOnClickListener;
    }

    /**
     * Set Butter item and centre button long click
     *
     * @param butterBarOnLongClickListener Buter Bar long click listener
     */
    public void setButterBarOnLongClickListener(ButterBarOnLongClickListener butterBarOnLongClickListener) {
        this.butterBarOnLongClickListener = butterBarOnLongClickListener;
    }

    /**
     * Change current selected item to given index
     *
     * @param indexToChange given index
     */
    public void changeCurrentItem(int indexToChange) {
        if (indexToChange < 0 || indexToChange > butterBarItems.size())
            throw new ArrayIndexOutOfBoundsException("Please be more careful, we do't have such item : " + indexToChange);
        else {
            updateButterBarItems(indexToChange);
        }
    }

    /**
     * Show badge at index
     *
     * @param itemIndex index
     * @param badgeText badge count text
     */
    public void showBadgeAtIndex(int itemIndex, long badgeText, @ColorInt int badgeColor) {
        if (itemIndex < 0 || itemIndex > butterBarItems.size()) {
            throwArrayIndexOutOfBoundsException(itemIndex);
        } else {
            try {
                RelativeLayout badgeView = badgeList.get(itemIndex);
                /**
                 * Set circle background to badge view
                 */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    badgeView.setBackground(BadgeHelper.makeShapeDrawable(badgeColor));
                } else {
                    badgeView.setBackgroundDrawable(BadgeHelper.makeShapeDrawable(badgeColor));
                }
                BadgeItem badgeItem = new BadgeItem(itemIndex, badgeText, badgeColor);
                BadgeHelper.showBadge(badgeView, badgeItem, shouldShowBadgeWithNinePlus);
                badgeSaveInstanceHashMap.put(itemIndex, badgeItem);
            } catch (IndexOutOfBoundsException e) {

            }
        }
    }


    public View getViewAtIndex(int index) {
        return butterBarItemList.get(index);
    }

    /**
     * Hide badge at index
     *
     * @param index badge index
     */
    public void hideBudgeAtIndex(final int index) {
        if (badgeList.get(index).getVisibility() == GONE) {
            Log.d(TAG, "Budge at index: " + index + " already hidden");
        } else {
            BadgeHelper.hideBadge(badgeList.get(index));
            badgeSaveInstanceHashMap.remove(index);
        }
    }

    /**
     * Hiding all available badges
     */
    public void hideAllBudges() {
        for (RelativeLayout badge : badgeList) {
            if (badge.getVisibility() == VISIBLE)
                BadgeHelper.hideBadge(badge);
        }
        badgeSaveInstanceHashMap.clear();
    }

    /**
     * Change badge text at index
     *
     * @param badgeIndex target index
     * @param badgeText  badge count text to change
     */
    public void changeBadgeTextAtIndex(int badgeIndex, int badgeText) {
        if (badgeSaveInstanceHashMap.get(badgeIndex) != null &&
                (((BadgeItem) badgeSaveInstanceHashMap.get(badgeIndex)).getIntBadgeText() != badgeText)) {
            BadgeItem currentBadgeItem = (BadgeItem) badgeSaveInstanceHashMap.get(badgeIndex);
            BadgeItem badgeItemForSave = new BadgeItem(badgeIndex, badgeText, currentBadgeItem.getBadgeColor());
            BadgeHelper.forceShowBadge(
                    badgeList.get(badgeIndex),
                    badgeItemForSave,
                    shouldShowBadgeWithNinePlus);
            badgeSaveInstanceHashMap.put(badgeIndex, badgeItemForSave);
        }
    }

    /**
     * Set custom font for butter bar item textView
     *
     * @param customFont custom font
     */
    public void setFont(Typeface customFont) {
        isCustomFont = true;
        this.customFont = customFont;
    }

    public void setCentreButtonIconColorFilterEnabled(boolean enabled) {
        isCentreButtonIconColorFilterEnabled = enabled;
    }

    /**
     * Change centre button icon if butter bar navigation already set up
     *
     * @param icon Target icon to change
     */
    public void changeCenterButtonIcon(int icon) {
        if (fab == null) {
            Log.e(TAG, "You should call setCentreButtonIcon() instead, " +
                    "changeCenterButtonIcon works if butter bar navigation already set up");
        } else {
            fab.setImageResource(icon);
            centreButtonIcon = icon;
        }
    }

    /**
     * Change item icon if butter bar navigation already set up
     *
     * @param itemIndex Target position
     * @param newIcon   Icon to change
     */
    public void changeItemIconAtPosition(int itemIndex, int newIcon) {
        if (itemIndex < 0 || itemIndex > butterBarItems.size()) {
            throwArrayIndexOutOfBoundsException(itemIndex);
        } else {
            ButterBarItem butterBarItem = butterBarItems.get(itemIndex);
            RelativeLayout textAndIconContainer = (RelativeLayout) butterBarItemList.get(itemIndex);
            ImageView butterBarItemIcon = (ImageView) textAndIconContainer.findViewById(R.id.butter_bar_icon);
            butterBarItemIcon.setImageResource(newIcon);
            butterBarItem.setItemIcon(newIcon);
            changedItemAndIconHashMap.put(itemIndex, butterBarItem);
        }
    }

    /**
     * Change item text if Butter bar navigation already set up
     *
     * @param itemIndex Target position
     * @param newText   Text to change
     */
    public void changeItemTextAtPosition(int itemIndex, String newText) {
        if (itemIndex < 0 || itemIndex > butterBarItems.size()) {
            throwArrayIndexOutOfBoundsException(itemIndex);
        } else {
            ButterBarItem butterBarItem = butterBarItems.get(itemIndex);
            RelativeLayout textAndIconContainer = (RelativeLayout) butterBarItemList.get(itemIndex);
            TextView butterBarItemIcon = (TextView) textAndIconContainer.findViewById(R.id.butter_bar_text);
            butterBarItemIcon.setText(newText);
            butterBarItem.setItemName(newText);
            changedItemAndIconHashMap.put(itemIndex, butterBarItem);
        }
    }

    /**
     * Change Butter bar background color if Butter bar view already set up
     *
     * @param color Target color to change
     */
    public void changeButterBarBackgroundColor(@ColorInt int color) {
        if (color == butterBarBackgroundColor) {
            Log.d(TAG, "changeButterBarBackgroundColor: color already changed");
            return;
        }

        butterBarBackgroundColor = color;
        setBackgroundColors();
        centreContent.changeBackgroundColor(color);
    }


    /**
     * If you want to show full badge text or show 9+
     *
     * @param shouldShowBadgeWithNinePlus false for full text
     */
    public void shouldShowFullBadgeText(boolean shouldShowBadgeWithNinePlus) {
        this.shouldShowBadgeWithNinePlus = shouldShowBadgeWithNinePlus;
    }

    /**
     * set centre button color
     *
     * @param color target color
     */
    public void setCentreButtonIconColor(@ColorInt int color) {
        centreButtonIconColor = color;
    }

    public void setActiveItem(int index) {
        updateButterBarItems(index);
    }

}
