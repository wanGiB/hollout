package com.wan.hollout.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.wan.hollout.R;
import com.wan.hollout.ui.widgets.EasePhotoView;
import com.wan.hollout.ui.widgets.PhotoViewAttacher;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.UiUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * download and show original photoView
 */
public class EaseShowImageActivity extends Activity {

    @BindView(R.id.image)
    EasePhotoView photoView;

    @BindView(R.id.pb_load_local)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ease_activity_show_big_image);
        ButterKnife.bind(this);
        int default_res = getIntent().getIntExtra("default_image", R.drawable.ease_default_image);
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            String remotePath = intentExtras.getString(AppConstants.FILE_PATH);
            if (remotePath != null) {
                UiUtils.loadImage(this, remotePath, photoView);
                UiUtils.showView(progressBar, false);
            } else {
                photoView.setImageResource(default_res);
            }
            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    finish();
                }
            });
        }
    }

}
