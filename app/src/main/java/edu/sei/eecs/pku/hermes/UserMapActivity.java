package edu.sei.eecs.pku.hermes;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.BikingRouteOverlay;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.SuggestAddrInfo;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.sei.eecs.pku.hermes.configs.Constants;
import edu.sei.eecs.pku.hermes.model.Order;
import edu.sei.eecs.pku.hermes.utils.network.GsonRequest;
import edu.sei.eecs.pku.hermes.utils.network.HttpClientRequest;
import edu.sei.eecs.pku.hermes.utils.network.LocationGson;
import edu.sei.eecs.pku.hermes.utils.network.OrderListGson;
import edu.sei.eecs.pku.hermes.utils.network.ResultGson;
import info.hoang8f.widget.FButton;

@EActivity(R.layout.activity_map)
public class UserMapActivity extends AppCompatActivity implements BaiduMap.OnMapClickListener,
        OnGetRoutePlanResultListener {


    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.5f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.5f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;


    private boolean isTopTitleVisible = false;
    private boolean isBottomTitleVisible = true;

    // 浏览路线节点相关
    @ViewById(R.id.pre)
    FButton mBtnPre; // 上一个节点
    @ViewById(R.id.next)
    FButton mBtnNext; // 下一个节点
    int nodeIndex = -1; // 节点索引,供浏览节点时使用
    RouteLine route = null;
    OverlayManager routeOverlay = null;
    boolean useDefaultIcon = false;
    private TextView popupText = null; // 泡泡view

    // 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
    // 如果不处理touch事件，则无需继承，直接使用MapView即可
    @ViewById(R.id.map)
    MapView mMapView;    // 地图View
    BaiduMap mBaidumap = null;

    // 定位相关
//    public LocationClient mLocClient;
//    public MyLocationListener myListener = new MyLocationListener();
    private boolean isFirstLoc = true; // 是否首次定位
//    private static LatLng LatLng_start;


    @ViewById(R.id.scroll)
    NestedScrollView scrollView;
    @ViewById(R.id.fab)
    FloatingActionButton fab;

    @ViewById(R.id.container)
    CoordinatorLayout container;

    @ViewById(R.id.app_bar)
    AppBarLayout appbar;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @ViewById(R.id.bottom_title)
    LinearLayout bottomTitle;

    @ViewById(R.id.top_title)
    TextView topTitle;

    @ViewById(R.id.title_contact)
    TextView titleContact;

    @ViewById(R.id.title_address)
    TextView titleAddress;

    @ViewById(R.id.title_appointment)
    TextView titleArrive;

    @ViewById(R.id.title_wait)
    TextView titleWait;

    Order current;
    String courier_id;

    private LatLng currentLocation = null;

    private RequestQueue queue;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(this.getApplication());
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            current = (Order) bundle.get("current");
            courier_id = current.getCourierId();
        } else {
            current = new Order("-1", "北京");
            courier_id = "-1";
        }

        queue = HttpClientRequest.getInstance(this.getApplicationContext()).getRequestQueue();
    }

    @AfterViews
    public void init() {

        setupActionBar();
        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int maxScroll = appBarLayout.getTotalScrollRange();
                float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;

                handleAlphaOnTitle(percentage);
                handleToolbarTitleVisibility(percentage);
            }
        });
        startAlphaAnimation(topTitle, 0, View.INVISIBLE);

        titleContact.setText(getResources().getString(R.string.title_contact_map,
                current.getRecipientName(), current.getRecipientPhone()));
//        titleAddress.setText(current.getAddress());
        titleAddress.setVisibility(View.GONE);
        titleArrive.setText(getResources().getString(R.string.title_arrive_map,
                current.getEstimation()));
//        titleWait.setText(getResources().getString(R.string.title_wait_map,
//                current.getWaitTime()));
        titleWait.setVisibility(View.GONE);
        topTitle.setText(getResources().getString(R.string.title_contact_map,
                current.getRecipientName(), current.getRecipientPhone()));


        // 初始化地图
        mBaidumap = mMapView.getMap();
        mBaidumap.setMyLocationEnabled(true);
        MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        mBaidumap
                .setMyLocationConfigeration(new MyLocationConfiguration(
                        mCurrentMode, true, null));
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        // 地图点击事件处理
        mBaidumap.setOnMapClickListener(this);
        // 初始化搜索模块，注册事件监听



        // prevent scrollview from intercepting the touch event of baidu map
        View v = mMapView.getChildAt(0);
        v.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    scrollView.requestDisallowInterceptTouchEvent(false);
                }else{
                    scrollView.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findCourier(courier_id);
            }
        });

        Runnable courierLocatingRunnable = new Runnable() {
            @Override
            public void run() {
                findCourier(courier_id);
            }
        };
        ScheduledExecutorService service = Executors
                .newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(courierLocatingRunnable, 0, 180, TimeUnit.SECONDS);

    }

    private void findCourier(String courier_id) {

        Log.d("fundcourier", String.format(Locale.SIMPLIFIED_CHINESE, Constants.LOC_URL, courier_id));
        Snackbar.make(container, "寻找快递员中...", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        GsonRequest gsonRequest = new GsonRequest.RequestBuilder()
//                .post()
                .url(String.format(Locale.SIMPLIFIED_CHINESE, Constants.LOC_URL, courier_id))
                .clazz(LocationGson.class)
                .successListener(new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        if (((LocationGson)response).status.equals("ok")) {
                            currentLocation = new LatLng(((LocationGson)response).getLat(), ((LocationGson)response).getLon());
                            MyLocationData locData = new MyLocationData.Builder()
                                    .accuracy(10).direction(100)
                                    .latitude(currentLocation.latitude)
                                    .longitude(currentLocation.longitude).build();
                            mBaidumap.setMyLocationData(locData);
                            zoomToPoint(currentLocation);
                            Snackbar.make(container, "快递员定位完毕: " +
                                    currentLocation.longitude + "," +
                                    currentLocation.latitude,
                                    Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                })
                .errorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("bg_location", "upload error: " + error.getMessage());
                        Snackbar.make(container, "定位失败，请检查网络连接", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                })
                .build();
        queue.add(gsonRequest);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {

        setSupportActionBar(toolbar);
        setTitle("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if(!isTopTitleVisible) {
                startAlphaAnimation(topTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                isTopTitleVisible = true;
            }

        } else {

            if (isTopTitleVisible) {
                startAlphaAnimation(topTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                isTopTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(isBottomTitleVisible) {
                startAlphaAnimation(bottomTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                isBottomTitleVisible = false;
            }

        } else {
            if (!isBottomTitleVisible) {
                startAlphaAnimation(bottomTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                isBottomTitleVisible = true;
            }
        }
    }

    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }



    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(UserMapActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaidumap);
            mBaidumap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {

        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(UserMapActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaidumap);
            mBaidumap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(UserMapActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);
            routeOverlay = overlay;
            mBaidumap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
        if (bikingRouteResult == null || bikingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Snackbar.make(container, "抱歉，未找到结果", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        if (bikingRouteResult != null && bikingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
//            SuggestAddrInfo suggest = bikingRouteResult.getSuggestAddrInfo();
            return;
        }
        if (bikingRouteResult != null && bikingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            route = bikingRouteResult.getRouteLines().get(0);
            BikingRouteOverlay overlay = new MyBikingRouteOverlay(mBaidumap);
            routeOverlay = overlay;
            mBaidumap.setOnMarkerClickListener(overlay);
            overlay.setData(bikingRouteResult.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
            Snackbar.make(container, getResources().getString(R.string.snackbar_address), Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
    }

    // 定制RouteOverly
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyTransitRouteOverlay extends TransitRouteOverlay {

        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyBikingRouteOverlay extends BikingRouteOverlay {
        public  MyBikingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }


    }

    @Override
    public void onMapClick(LatLng point) {
        mBaidumap.hideInfoWindow();
    }

    @Override
    public boolean onMapPoiClick(MapPoi poi) {
        return false;
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
//        mSearch.destroy();
        mMapView.onDestroy();
//        mLocClient.stop();
        super.onDestroy();
    }

    private void zoomToPoint(LatLng ll) {

//        MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
//
//        mBaidumap
//                .setMyLocationConfigeration(new MyLocationConfiguration(
//                        mCurrentMode, true, null));

        if (ll != null) {
            MapStatus.Builder builder = new MapStatus.Builder();
            LatLng l = new LatLng(ll.latitude - 0.001, ll.longitude);

            builder.target(l).zoom(18.0f);
            mBaidumap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }

//        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
//        mBaidumap
//                .setMyLocationConfigeration(new MyLocationConfiguration(
//                        mCurrentMode, true, null));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
                break;
        }
        return true;
    }
}

