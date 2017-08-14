package com.wan.hollout.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;


import com.wan.hollout.R;
import com.wan.hollout.rendering.StickyRecyclerHeadersDecoration;
import com.wan.hollout.ui.adapters.UserVideosAdapter;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * * Created by Wan on 6/5/2016.
 */
@SuppressWarnings("unused")
public class UserVideosFragment extends Fragment {

    @BindView(R.id.user_videos_content_flipper)
    public ViewFlipper userVideosContentFlipper;
    @BindView(R.id.user_videos_recycler_view)
    public RecyclerView userVideosRecyclerView;
    public LinearLayoutManager linearLayoutManager;

    public UserVideosAdapter userVideosAdapter;

    public ArrayList<HolloutUtils.MediaEntry> userVideoEntries;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View userPhotosView = inflater.inflate(R.layout.fragment_user_videos, container, false);
        ButterKnife.bind(this, userPhotosView);
        return userPhotosView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        userVideosRecyclerView.setLayoutManager(linearLayoutManager);
        loadUserVideos();
    }

    public void loadUserVideos() {

        userVideoEntries = HolloutUtils.getSortedVideos(getActivity());
        Collections.sort(userVideoEntries, mediaSorter);

        userVideosAdapter = new UserVideosAdapter(getActivity(), userVideoEntries);
        StickyRecyclerHeadersDecoration stickyRecyclerHeadersDecoration = new StickyRecyclerHeadersDecoration(userVideosAdapter);
        userVideosRecyclerView.addItemDecoration(stickyRecyclerHeadersDecoration);
        userVideosRecyclerView.setAdapter(userVideosAdapter);

        if (!userVideoEntries.isEmpty()) {
            UiUtils.toggleFlipperState(userVideosContentFlipper, 1);
        } else {
            UiUtils.toggleFlipperState(userVideosContentFlipper, 2);
        }

    }

}
