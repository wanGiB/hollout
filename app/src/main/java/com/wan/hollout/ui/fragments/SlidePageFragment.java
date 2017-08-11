package com.wan.hollout.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wan.hollout.R;
import com.wan.hollout.utils.UiUtils;


/**
 * @author Wan Clem
 */
public class SlidePageFragment extends Fragment {
    private static final String PIC_URL = "slidepagefragment.picurl";

    public static SlidePageFragment newInstance(@NonNull final String picUrl) {
        Bundle arguments = new Bundle();
        arguments.putString(PIC_URL, picUrl);
        SlidePageFragment fragment = new SlidePageFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_slide_page, container, false);
        ImageView view = (ImageView) rootView.findViewById(R.id.pic);
        Bundle arguments = getArguments();
        if (arguments != null) {
            String url = arguments.getString(PIC_URL);
            UiUtils.loadImage(getActivity(), url, view);
        }
        return rootView;
    }
}
