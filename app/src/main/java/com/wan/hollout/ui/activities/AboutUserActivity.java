package com.wan.hollout.ui.activities;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.ui.widgets.HolloutRoundedImageCorner;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class AboutUserActivity extends BaseActivity implements ATEActivityThemeCustomizer {

    private boolean isDarkTheme;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.pick_photo_view)
    ImageView userPhotoView;

    @BindView(R.id.interests_suggestion_recycler_view)
    RecyclerView interestsSuggestionRecyclerView;

    @BindView(R.id.more_about_user_field)
    EditText moreAboutUserField;

    @BindView(R.id.reason_for_interests_view)
    HolloutTextView reasonForInterestsView;

    @BindView(R.id.button_continue)
    HolloutTextView buttonContinue;

    private ParseUser signedInUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isDarkTheme = HolloutPreferences.getHolloutPreferences().getBoolean("dark_theme", false);
        setContentView(R.layout.about_user_layout);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        signedInUser = ParseUser.getCurrentUser();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        loadSignedInUserPhoto();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadSignedInUserPhoto() {
        if (signedInUser != null) {
            String signedInUserPhotoUrl = signedInUser.getString(AppConstants.USER_PHOTO_URL);
            if (StringUtils.isNotEmpty(signedInUserPhotoUrl)) {
                UiUtils.loadImage(this, signedInUserPhotoUrl, userPhotoView);
            }
        }
    }

    @Override
    public int getActivityTheme() {
        return isDarkTheme ? R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
    }

}
