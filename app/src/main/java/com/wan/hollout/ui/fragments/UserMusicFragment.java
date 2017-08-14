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
import com.wan.hollout.bean.AudioFile;
import com.wan.hollout.eventbuses.ClearSelectedAudio;
import com.wan.hollout.rendering.StickyRecyclerHeadersDecoration;
import com.wan.hollout.ui.activities.GalleryActivity;
import com.wan.hollout.ui.adapters.UserMusicAdapter;
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
 * * Created by Wan on 6/5/2016.
 */
@SuppressWarnings("unused")
public class UserMusicFragment extends Fragment {

    @BindView(R.id.user_music_content_flipper)
    public ViewFlipper userMusicContentFlipper;
    @BindView(R.id.user_music_recycler_view)
    public RecyclerView userMusicRecyclerView;
    public LinearLayoutManager linearLayoutManager;
    public static UserMusicAdapter userMusicAdapter;
    public ArrayList<AudioFile> musicFiles;

    private Comparator<AudioFile> mediaSorter = new Comparator<AudioFile>() {
        @Override
        public int compare(AudioFile o1, AudioFile o2) {
            if (o1 != null && o2 != null) {
                if (o1.getAuthor() != null && o2.getAuthor() != null) {
                    return o2.getAuthor().compareTo(o1.getAuthor());
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
        View userPhotosView = inflater.inflate(R.layout.fragment_user_music, container, false);
        ButterKnife.bind(this, userPhotosView);
        return userPhotosView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        userMusicRecyclerView.setLayoutManager(linearLayoutManager);
        loadUserMusicFiles();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof ClearSelectedAudio) {
                    ClearSelectedAudio clearSelectedAudio = (ClearSelectedAudio) o;
                    if (clearSelectedAudio.canClearSelectedAudio()) {
                        userMusicAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    public void loadUserMusicFiles() {

        musicFiles = HolloutUtils.getSortedMusicFiles(getActivity());
        Collections.sort(musicFiles, mediaSorter);

        userMusicAdapter = new UserMusicAdapter((GalleryActivity) getActivity(), musicFiles);
        StickyRecyclerHeadersDecoration stickyRecyclerHeadersDecoration = new StickyRecyclerHeadersDecoration(userMusicAdapter);
        userMusicRecyclerView.addItemDecoration(stickyRecyclerHeadersDecoration);
        userMusicRecyclerView.setAdapter(userMusicAdapter);
        if (!musicFiles.isEmpty()) {
            UiUtils.toggleFlipperState(userMusicContentFlipper, 1);
        } else {
            UiUtils.toggleFlipperState(userMusicContentFlipper, 2);
        }
    }

}
