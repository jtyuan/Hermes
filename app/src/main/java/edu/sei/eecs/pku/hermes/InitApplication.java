package edu.sei.eecs.pku.hermes;

import android.app.Application;
import android.content.SharedPreferences;

import org.androidannotations.annotations.EApplication;

/**
 * Created by bilibili on 15/12/11.
 */
@EApplication
public class InitApplication extends Application {


    public SharedPreferences loginInfo;

    public static String courier_id;

    @Override
    public void onCreate() {
        super.onCreate();


        loginInfo = getSharedPreferences("login", MODE_PRIVATE);
        courier_id = loginInfo.getString("courier_id", "null");
    }

}
