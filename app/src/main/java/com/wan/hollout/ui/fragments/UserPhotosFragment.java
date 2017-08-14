package com.wan.hollout.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;


import com.wan.hollout.R;
import com.wan.hollout.eventbuses.ClearSelectedPhotos;
import com.wan.hollout.ui.activities.GalleryActivity;
import com.wan.hollout.ui.adapters.UserPhotosAdapter;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * * Created by Wan on 3/18/2016.
 */
@SuppressWarnings("unused")
public class UserPhotosFragment extends Fragment {

    @BindView(R.id.user_photos_content_flipper)
    public ViewFlipper userPhotosContentFlipper;

    @BindView(R.id.user_photos_grid)
    public RecyclerView userPhotosRecyclerView;

    public static UserPhotosAdapter userPhotosAdapter;
    public ArrayList<HolloutUtils.MediaEntry> userPhotos;

    private Comparator<HolloutUtils.MediaEntry> mediaSorter = new Comparator<HolloutUtils.MediaEntry>() {
        @Override
        public int compare(HolloutUtils.MediaEntry o1, HolloutUtils.MediaEntry o2) {
            if (o1 != null && o2 != null) {
                if (o1.bucketName != null && o2.bucketName != null) {
                    return o2.bucketName.compareTo(o1.bucketName);
                } else {
                    return -1;
                }
            } else {
                return -1;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View userPhotosView = inflater.inflate(R.layout.fragment_user_photos, container, false);
        ButterKnife.bind(this, userPhotosView);
        return userPhotosView;
    }

    private void loadPhotos() {

        userPhotos = HolloutUtils.getSortedPhotos(getActivity());

        Collections.sort(userPhotos, mediaSorter);

        userPhotosAdapter = new UserPhotosAdapter((GalleryActivity) getActivity(), userPhotos);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        userPhotosAdapter.setGridLayoutManager(gridLayoutManager);

        userPhotosRecyclerView.setLayoutManager(gridLayoutManager);

        userPhotosRecyclerView.setAdapter(userPhotosAdapter);
        if (userPhotos.isEmpty()) {
            UiUtils.toggleFlipperState(userPhotosContentFlipper, 2);
        } else {
            UiUtils.toggleFlipperState(userPhotosContentFlipper, 1);
        }

    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof ClearSelectedPhotos) {
                    ClearSelectedPhotos clearSelectedPhotos = (ClearSelectedPhotos) o;
                    if (clearSelectedPhotos.canClearSelectedPhotos()) {
                        userPhotosAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadPhotos();
    }

}
