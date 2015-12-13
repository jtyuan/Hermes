package edu.sei.eecs.pku.hermes;

import android.app.Application;
import android.util.Log;

import org.androidannotations.annotations.EApplication;

import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import edu.sei.eecs.pku.hermes.utils.PushUtil;

/**
 * Created by bilibili on 15/12/11.
 */
@EApplication
public class InitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

}
