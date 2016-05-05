package edu.sei.eecs.pku.hermes;

import android.app.Application;
import android.content.SharedPreferences;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import org.androidannotations.annotations.EApplication;

import edu.sei.eecs.pku.hermes.configs.Constants;
import edu.sei.eecs.pku.hermes.utils.network.BackgroundLocationListener;

/**
 * Created by bilibili on 15/12/11.
 */
@EApplication
public class InitApplication extends Application {


    public SharedPreferences loginInfo;

    public static String courier_id;
    private boolean is_courier;

    public Application instance;


    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        loginInfo = getSharedPreferences("login", MODE_PRIVATE);
        courier_id = loginInfo.getString("courier_id", "");
        is_courier = loginInfo.getBoolean("isCourier", false);


    }

}
