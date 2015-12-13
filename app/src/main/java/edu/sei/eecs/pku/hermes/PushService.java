package edu.sei.eecs.pku.hermes;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import org.androidannotations.annotations.EService;

import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import edu.sei.eecs.pku.hermes.utils.PushReceiver;
import edu.sei.eecs.pku.hermes.utils.PushUtil;

@EService
public class PushService extends Service {

    private static final String TAG = "JPush";

    public PushService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);     		// 初始化 JPush

        Log.d(TAG, "Set alias in handler.");
        // TODO: change to real courier id
        JPushInterface.setAliasAndTags(getApplicationContext(), String.valueOf(25430), null, mAliasCallback);
    }


    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs ;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    Log.i(TAG, logs);
                    break;

                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    Log.i(TAG, logs);
                    if (PushUtil.isConnected(getApplicationContext())) {
                        // TODO: change to real courier id
                        JPushInterface.setAliasAndTags(getApplicationContext(), String.valueOf(25430), null, mAliasCallback);
                    } else {
                        Log.i(TAG, "No network");
                    }
                    break;

                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e(TAG, logs);
            }

            PushUtil.showToast(logs, getApplicationContext());
        }

    };
}
