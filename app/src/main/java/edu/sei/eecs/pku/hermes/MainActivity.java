package edu.sei.eecs.pku.hermes;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
//
//    @ViewById(R.id.toolbar)
//    Toolbar toolbar;
//
//    @ViewById(R.id.fab)
//    FloatingActionButton fab;

    MapView mMapView = null;

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


        SDKInitializer.initialize(getApplicationContext());

        this.startService(new Intent(this, PushService.class));
        PushService_.intent(getApplication()).start();

//        MapActivity_.intent(MainActivity.this).start();
        // TODO: change to login activity
//        TodayActivity_.intent(MainActivity.this).start();
        LoginActivity_.intent(MainActivity.this).start();
//        PlanResultActivity_.intent(MainActivity.this).start();
        finish();
    }
}
