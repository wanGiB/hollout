package com.wan.hollout.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.FirebaseUtils;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public abstract class BaseActivity extends AppCompatActivity {

    private DatabaseReference timeStampValueRef;
    private ValueEventListener timeStampValueEventListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        checkAndRegEventBus();
        if (!isTaskRoot()) {
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                        finish();
                    }
                }
            }
        }
        stayInSyncWithServer();
    }

    private void stayInSyncWithServer() {
        if (timeStampValueRef != null && timeStampValueEventListener != null) {
            timeStampValueRef.removeEventListener(timeStampValueEventListener);
        }
        timeStampValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.exists()) {
                    Long receivedTime = dataSnapshot.getValue(Long.class);
                    HolloutLogger.d("ReceivedTimeTag", "Received Time From Server= " + receivedTime);
                    ParseObject signedInUser = AuthUtil.getCurrentUser();
                    if (signedInUser != null) {
                        signedInUser.put(AppConstants.USER_CURRENT_TIME_STAMP, receivedTime);
                        signedInUser.put(AppConstants.APP_USER_ONLINE_STATUS, AppConstants.ONLINE);
                        signedInUser.put(AppConstants.APP_USER_LAST_SEEN, receivedTime);
                        AuthUtil.updateCurrentLocalUser(signedInUser, null);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        };
        timeStampValueRef = FirebaseUtils.getServerUpTimeRef();
        timeStampValueRef.addValueEventListener(timeStampValueEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndRegEventBus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRegEventBus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkAndUnRegEventBus();
        if (timeStampValueRef != null && timeStampValueEventListener != null) {
            timeStampValueRef.removeEventListener(timeStampValueEventListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        checkAndUnRegEventBus();
    }

    private void checkAndRegEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void checkAndUnRegEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public BaseActivity getCurrentActivityInstance() {
        return this;
    }

}
