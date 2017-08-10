package com.wan.hollout.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.skyfishjy.library.RippleBackground;
import com.wan.hollout.R;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class ChatsFragment extends Fragment {

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

    @BindView(R.id.content_flipper)
    ViewFlipper contentFlipper;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.people_recycler_view)
    RecyclerView peopleRecyclerView;

    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View peopleView = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, peopleView);
        return peopleView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UiUtils.setUpRefreshColorSchemes(getActivity(), swipeRefreshLayout);
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
        checkAndRegEventBus();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndRegEventBus();
    }

    @Override
    public void onStop() {
        super.onStop();
        checkAnUnRegEventBus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        checkAnUnRegEventBus();
    }

    private void checkAndRegEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void checkAnUnRegEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void delayAndAnimateFoundView(final View view) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                foundDevice(view);
            }
        }, 3000);
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof String) {
                    String message = (String) o;
                    if (message.equals(AppConstants.JUST_AUTHENTICATED)) {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser == null) {
                            UiUtils.toggleFlipperState(contentFlipper, 0);
                        }
                    }
                }
            }
        });
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
