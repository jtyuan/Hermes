package edu.sei.eecs.pku.hermes.utils.network;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.model.LatLng;

import java.util.Locale;

import edu.sei.eecs.pku.hermes.InitApplication;
import edu.sei.eecs.pku.hermes.configs.Constants;

/**
 * Created by bilibili on 16/5/5.
 */
public class BackgroundLocationListener implements BDLocationListener {

    private Context context;
    private RequestQueue queue;
    private LatLng currentLocation;

    public BackgroundLocationListener(Context context) {
        super();
        Log.d("bg_location", "Initialized");
        this.context = context;
        queue = HttpClientRequest.getInstance(context).getRequestQueue();
    }

    @Override
    public void onReceiveLocation(BDLocation location) {

        currentLocation = new LatLng(location.getLatitude(),
                location.getLongitude());

        Log.d("bg_location", "uploading to "
                + String.format(Locale.SIMPLIFIED_CHINESE, Constants.LOC_URL, InitApplication.courier_id));

        GsonRequest gsonRequest = new GsonRequest.RequestBuilder()
                .post()
                .url(String.format(Locale.SIMPLIFIED_CHINESE, Constants.LOC_URL, InitApplication.courier_id))
                .addParams("lat", String.valueOf(currentLocation.latitude))
                .addParams("lon", String.valueOf(currentLocation.longitude))
                .clazz(ResultGson.class)
                .successListener(new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        if (((ResultGson)response).status.equals("ok")) {
                            Log.d("bg_location", "upload success");
                        }
                    }
                })
                .errorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("bg_location", "upload error: " + error.getMessage());
                    }
                })
                .build();
        queue.add(gsonRequest);

    }
}