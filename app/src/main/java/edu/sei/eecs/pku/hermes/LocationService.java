package edu.sei.eecs.pku.hermes;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;

import org.androidannotations.annotations.EService;

import edu.sei.eecs.pku.hermes.configs.Constants;
import edu.sei.eecs.pku.hermes.utils.network.BackgroundLocationListener;

@EService
public class LocationService extends Service {

    public static LocationService instance;

    // 定位相关
    public LocationClient mLocClient;
    public BackgroundLocationListener myListener;

    public LocationService() {
        instance = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(getApplication());

        myListener = new BackgroundLocationListener(getApplicationContext());
        mLocClient = new LocationClient(getApplication());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(Constants.LOCATION_INTERVAL_BG);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocClient.stop();
    }
}
