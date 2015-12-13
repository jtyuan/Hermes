package edu.sei.eecs.pku.hermes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import cn.jpush.android.api.JPushInterface;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
//
//    @ViewById(R.id.toolbar)
//    Toolbar toolbar;
//
//    @ViewById(R.id.fab)
//    FloatingActionButton fab;

    @Click(R.id.fab)
    void fabClicked(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setSupportActionBar(toolbar);
//        JPushInterface.init(getApplicationContext());

        this.startService(new Intent(this,PushService.class));
        PushService_.intent(getApplication()).start();

        // TODO: change to login activity
        TodayActivity_.intent(MainActivity.this).start();
        finish();
    }

}
