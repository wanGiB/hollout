package com.wan.hollout.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.skyfishjy.library.RippleBackground;
import com.wan.hollout.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class PeopleFragment extends Fragment {

    @BindView(R.id.ripple_background)
    RippleBackground rippleBackground;

    @BindView(R.id.first_person)
    ImageView firstPerson;

    @BindView(R.id.second_person)
    ImageView secondPerson;

    @BindView(R.id.third_person)
    ImageView thirdPerson;

    @BindView(R.id.fourth_person)
    ImageView fourthPerson;

    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View peopleView = inflater.inflate(R.layout.fragment_people, container, false);
        ButterKnife.bind(this, peopleView);
        return peopleView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rippleBackground.startRippleAnimation();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                foundDevice(firstPerson);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        delayAndAnimateFoundView(secondPerson);
                    }
                }, 3000);
            }
        }, 3000);
    }

    private void delayAndAnimateFoundView(final View view) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                foundDevice(view);
            }
        }, 3000);
    }

    private void foundDevice(View foundDevice) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(400);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        ArrayList<Animator> animatorList = new ArrayList<>();
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(foundDevice, "ScaleX", 0f, 1.2f, 1f);
        animatorList.add(scaleXAnimator);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(foundDevice, "ScaleY", 0f, 1.2f, 1f);
        animatorList.add(scaleYAnimator);
        animatorSet.playTogether(animatorList);
        foundDevice.setVisibility(View.VISIBLE);
        animatorSet.start();
    }

}
