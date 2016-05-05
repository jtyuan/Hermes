package edu.sei.eecs.pku.hermes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.baidu.mapapi.map.Text;
import com.cengalabs.flatui.views.FlatToggleButton;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.Holder;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;
import com.yalantis.phoenix.PullToRefreshView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import edu.sei.eecs.pku.hermes.configs.Constants;
import edu.sei.eecs.pku.hermes.model.Failure;
import edu.sei.eecs.pku.hermes.model.Order;
import edu.sei.eecs.pku.hermes.model.User;
import edu.sei.eecs.pku.hermes.utils.adapters.FailureAdapter;
import edu.sei.eecs.pku.hermes.utils.adapters.TodayAdapter;
import edu.sei.eecs.pku.hermes.utils.network.GsonRequest;
import edu.sei.eecs.pku.hermes.utils.network.HttpClientRequest;
import edu.sei.eecs.pku.hermes.utils.network.OrderListGson;
import edu.sei.eecs.pku.hermes.utils.network.ResultGson;

@EActivity(R.layout.activity_plan_result)
public class PlanResultActivity extends AppCompatActivity implements View.OnClickListener, Comparator<Order> {
    public static boolean isForeground = false;

    private SharedPreferences scheduleInfo;
    private SharedPreferences loginInfo;
    private long lastSchedule;

    private RequestQueue queue;

    private ArrayList<Order> orders;
    private ArrayList<Order> readyOrders;
    private ArrayList<Order> uninformedOrders;
    private ArrayList<Order> failedOrders;

    @Extra
    ArrayList<Order> completedOrders;

    ArrayList<User> users;

    List<TextView> currentCard;

    Order current;

    @ViewById(R.id.waitingList)
    ListView waitingList;

    @ViewById(R.id.uninformedList)
    ListView uninformedList;

    @ViewById(R.id.failedList)
    ListView failedList;

    @ViewById(R.id.tvCurrent)
    TextView tvCurrent;

    @ViewById(R.id.tvWaiting)
    TextView tvWaiting;

    @ViewById(R.id.tvUninformed)
    TextView tvUninformed;

    @ViewById(R.id.tvFailed)
    TextView tvFailed;

    @ViewById(R.id.tvOrderId)
    TextView tvOrderId;

    @ViewById(R.id.tvName)
    TextView tvName;

    @ViewById(R.id.tvAddress)
    TextView tvAddress;

    @ViewById(R.id.tvRes1)
    TextView tvArriveTime;

    @ViewById(R.id.tvEwt)
    TextView tvEwt;

    @ViewById(R.id.llHeaderProgress)
    LinearLayout llHeaderProgress;

    @ViewById(R.id.progressBar)
    CircleProgressBar progressBar;

    @ViewById(R.id.scrollView)
    ScrollView scrollView;

    @ViewById(R.id.pull_to_refresh)
    PullToRefreshView pullToRefreshView;

    ResideMenu resideMenu;

    private boolean isCourier = false;
    private ResideMenuItem itemHome;
    private ResideMenuItem itemToday;
    private ResideMenuItem itemHistory;
    private ResideMenuItem itemLogout;
    private ResideMenuItem itemMyList;
    private TodayAdapter adapter_wait;
    private TodayAdapter adapter_uninformed;
    private FailureAdapter adapter_failure;

    @Click
    void buttonDone() {

        Holder holder = new ViewHolder(R.layout.dialog_done);

        View headerView = View.inflate(PlanResultActivity.this, R.layout.detail_dialog_header, null);
        View footerView = View.inflate(PlanResultActivity.this, R.layout.detail_dialog_footer_two_buttons, null);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.CHINA);

        ((TextView)headerView.findViewById(R.id.tvHeaderTitle)).setText(R.string.detail_header_title);
        ((TextView)headerView.findViewById(R.id.tvHeaderSub)).setText("配送成功");
//        (footerView.findViewById(R.id.tvFooterTitle)).setVisibility(View.INVISIBLE);
        ((TextView)footerView.findViewById(R.id.tvFooterTitle)).setText(sdf.format(calendar.getTime()));


        boolean isChecked = false;
        DialogPlus dialog = DialogPlus.newDialog(PlanResultActivity.this)
                .setContentHolder(holder)
                .setHeader(headerView)
                .setFooter(footerView)
                .setGravity(Gravity.TOP)
                .setCancelable(true)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogPlus dialog, View view) {
                        switch (view.getId()) {
                            case R.id.footer_confirm_button:
                                View holderView = dialog.getHolderView();
                                FlatToggleButton toggle = (FlatToggleButton) holderView.findViewById(R.id.toggle);
                                if (toggle.isChecked()) {
                                    showProgress(true);
                                    GsonRequest gsonRequest = new GsonRequest.RequestBuilder()
                                            .url(Constants.BASE_URL)
                                            .addParams("delivery", readyOrders.get(0).getOrderId())
                                            .addParams("inform", String.valueOf(Constants.STATUS_COMPLETED))
                                            .clazz(ResultGson.class)
                                            .successListener(new Response.Listener() {
                                                @Override
                                                public void onResponse(Object response) {
                                                    if (((ResultGson) response).status.equals("ok")) {
                                                        completedOrders.add(readyOrders.get(0));
                                                        readyOrders.remove(0);
                                                        if (uninformedOrders.size() > 0) {
                                                            readyOrders.add(uninformedOrders.get(0));
                                                            uninformedOrders.remove(0);
                                                        }
                                                        refreshList();
                                                        showProgress(false);
                                                    }
                                                }
                                            })
                                            .errorListener(new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Toast.makeText(PlanResultActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                                                    showProgress(false);
                                                }
                                            })
                                            .build();
                                    queue.add(gsonRequest);
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(PlanResultActivity.this, "请确认是否送达收货人手中", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case R.id.footer_close_button:
                                dialog.dismiss();
                        }
                    }
                })
                .create();

        View holderView = dialog.getHolderView();

        ((TextView)holderView.findViewById(R.id.tvConfirm)).setText(getResources()
                .getString(R.string.dialog_done_confirm, current.getRecipientName()));
        (holderView.findViewById(R.id.toggle)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlatToggleButton toggle = (FlatToggleButton) v;
                if (((FlatToggleButton) v).isChecked()) {

                }
            }
        });
//
//        Calendar calendar = Calendar.getInstance();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.CHINA);
//        ((TextView)holderView.findViewById(R.id.tvCurrentTime)).setText(getResources()
//                .getString(R.string.dialog_done_time, sdf.format(calendar.getTime())));

        dialog.show();
    }

    @Click
    void buttonFailed() {
        final List<String> list = new ArrayList<>();

        list.add(PlanResultActivity.this.getString(R.string.failed_reason_1));
        list.add(PlanResultActivity.this.getString(R.string.failed_reason_2));
        list.add(PlanResultActivity.this.getString(R.string.failed_reason_3));
//        list.add(PlanResultActivity.this.getString(R.string.failed_reason_4));
//        list.add(PlanResultActivity.this.getString(R.string.failed_reason_5));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(PlanResultActivity.this,
                R.layout.spinner_item, list);

        Holder holder = new ViewHolder(R.layout.dialog_failed);

        View headerView = View.inflate(PlanResultActivity.this, R.layout.detail_dialog_header, null);
        View footerView = View.inflate(PlanResultActivity.this, R.layout.detail_dialog_footer_two_buttons, null);



        ((TextView)headerView.findViewById(R.id.tvHeaderTitle)).setText(R.string.failed_title);
        ((TextView)headerView.findViewById(R.id.tvHeaderSub)).setText(R.string.failed_sub_title);
        (footerView.findViewById(R.id.tvFooterTitle)).setVisibility(View.INVISIBLE);

        final TextView tvFailure = new TextView(this);
        DialogPlus dialog = DialogPlus.newDialog(PlanResultActivity.this)
                .setContentHolder(holder)
                .setHeader(headerView)
                .setFooter(footerView)
                .setGravity(Gravity.TOP)
                .setCancelable(true)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogPlus dialog, View view) {
                        switch (view.getId()) {
                            case R.id.footer_confirm_button:
                                showProgress(true);
                                GsonRequest gsonRequest = new GsonRequest.RequestBuilder()
//                                        .post()
                                        .url(Constants.BASE_URL)
                                        .addParams("delivery", readyOrders.get(0).getOrderId())
                                        .addParams("inform", tvFailure.getText().toString())
                                        .clazz(ResultGson.class)
                                        .successListener(new Response.Listener() {
                                            @Override
                                            public void onResponse(Object response) {
                                                if (((ResultGson) response).status.equals("ok")) {
                                                    readyOrders.get(0).setFailure(tvFailure.getText().toString());
                                                    failedOrders.add(readyOrders.get(0));
                                                    readyOrders.remove(0);
                                                    refreshList();
                                                }
                                                showProgress(false);
                                            }
                                        })
                                        .errorListener(new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Toast.makeText(PlanResultActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                                                showProgress(false);
                                            }
                                        })
                                        .build();
                                queue.add(gsonRequest);
                                dialog.dismiss();
                                break;
                            case R.id.footer_close_button:
                                dialog.dismiss();
                                break;
                        }
                    }
                })
                .create();

        Spinner spinner = (Spinner) dialog.getHolderView().findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tvFailure.setText(list.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dialog.show();
    }

    @ItemClick
    void waitingListItemClicked(Order clickItem) {
        Holder holder = new ViewHolder(R.layout.list_detail);

        View headerView = View.inflate(PlanResultActivity.this, R.layout.detail_dialog_header, null);
        View footerView = View.inflate(PlanResultActivity.this, R.layout.detail_dialog_footer_one_button, null);

        ((TextView)headerView.findViewById(R.id.tvHeaderTitle)).setText(R.string.detail_header_title);
        ((TextView)headerView.findViewById(R.id.tvHeaderSub)).setText(getResources()
                .getString(R.string.vip_rank, clickItem.getVIPRank()));
        (footerView.findViewById(R.id.tvFooterTitle)).setVisibility(View.INVISIBLE);

        DialogPlus dialog = DialogPlus.newDialog(PlanResultActivity.this)
                .setContentHolder(holder)
                .setHeader(headerView)
                .setFooter(footerView)
                .setGravity(Gravity.BOTTOM)
                .setCancelable(true)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogPlus dialog, View view) {
                        switch (view.getId()) {
                            case R.id.footer_confirm_button:
                                dialog.dismiss();
                                break;
                        }
                    }
                })
                .create();

        View holderView = dialog.getHolderView();
        ((TextView)holderView.findViewById(R.id.tvOrderId)).setText(clickItem.getOrderId());
        ((TextView)holderView.findViewById(R.id.tvName)).setText(clickItem.getRecipientName());
//        if (Math.random() >= 0.5) {
//            ((TextView) holderView.findViewById(R.id.tvGender)).setText("女士");
//        } else {
//            ((TextView) holderView.findViewById(R.id.tvGender)).setText("先生");
//        }
        ((TextView) holderView.findViewById(R.id.tvGender)).setText(""); // no gender data
        ((TextView)holderView.findViewById(R.id.tvPhone)).setText(clickItem.getRecipientPhone());
        ((TextView)holderView.findViewById(R.id.tvAddress)).setText(clickItem.getAddress());
        ((TextView)holderView.findViewById(R.id.tvRes1)).setText(clickItem.getReserveBegin());
        ((TextView)holderView.findViewById(R.id.tvRes2)).setText(clickItem.getReserveEnd());
        ((TextView)holderView.findViewById(R.id.tvEstimation)).setText(clickItem.getEstimation());

        dialog.show();
    }

    @ItemClick
    void uninformedListItemClicked(Order clickItem) {
        Holder holder = new ViewHolder(R.layout.list_detail_raw);

        View headerView = View.inflate(PlanResultActivity.this, R.layout.detail_dialog_header, null);
        View footerView = View.inflate(PlanResultActivity.this, R.layout.detail_dialog_footer_one_button, null);

        ((TextView)headerView.findViewById(R.id.tvHeaderTitle)).setText(R.string.detail_header_title);
        ((TextView)headerView.findViewById(R.id.tvHeaderSub)).setText(getResources()
                .getString(R.string.vip_rank, clickItem.getVIPRank()));
        (footerView.findViewById(R.id.tvFooterTitle)).setVisibility(View.INVISIBLE);

        DialogPlus dialog = DialogPlus.newDialog(PlanResultActivity.this)
                .setContentHolder(holder)
                .setHeader(headerView)
                .setFooter(footerView)
                .setGravity(Gravity.BOTTOM)
                .setCancelable(true)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogPlus dialog, View view) {
                        switch (view.getId()) {
                            case R.id.footer_confirm_button:
                                dialog.dismiss();
                                break;
                        }
                    }
                })
                .create();

        View holderView = dialog.getHolderView();
        ((TextView)holderView.findViewById(R.id.tvOrderId)).setText(clickItem.getOrderId());
        ((TextView)holderView.findViewById(R.id.tvName)).setText(clickItem.getRecipientName());
//        if (Math.random() >= 0.5) {
//            ((TextView) holderView.findViewById(R.id.tvGender)).setText("女士");
//        } else {
//            ((TextView) holderView.findViewById(R.id.tvGender)).setText("先生");
//        }
        ((TextView) holderView.findViewById(R.id.tvGender)).setText(""); // no gender data
        ((TextView)holderView.findViewById(R.id.tvPhone)).setText(clickItem.getRecipientPhone());
        ((TextView)holderView.findViewById(R.id.tvAddress)).setText(clickItem.getAddress());
        ((TextView)holderView.findViewById(R.id.tvRes1)).setText(clickItem.getReserveBegin());
        ((TextView)holderView.findViewById(R.id.tvRes2)).setText(clickItem.getReserveEnd());

        dialog.show();
    }

    @ItemClick
    void failedListItemClicked(Order clickItem) {
        Holder holder = new ViewHolder(R.layout.list_detail_raw);

        View headerView = View.inflate(PlanResultActivity.this, R.layout.detail_dialog_header, null);
        View footerView = View.inflate(PlanResultActivity.this, R.layout.detail_dialog_footer_one_button, null);

        ((TextView)headerView.findViewById(R.id.tvHeaderTitle)).setText(R.string.detail_header_title);
        ((TextView)headerView.findViewById(R.id.tvHeaderSub)).setText(getResources()
                .getString(R.string.vip_rank, clickItem.getVIPRank()));
        (footerView.findViewById(R.id.tvFooterTitle)).setVisibility(View.INVISIBLE);

        DialogPlus dialog = DialogPlus.newDialog(PlanResultActivity.this)
                .setContentHolder(holder)
                .setHeader(headerView)
                .setFooter(footerView)
                .setGravity(Gravity.BOTTOM)
                .setCancelable(true)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogPlus dialog, View view) {
                        switch (view.getId()) {
                            case R.id.footer_confirm_button:
                                dialog.dismiss();
                                break;
                        }
                    }
                })
                .create();

        View holderView = dialog.getHolderView();

        ((TextView)holderView.findViewById(R.id.tvOrderId)).setText(clickItem.getOrderId());
        ((TextView)holderView.findViewById(R.id.tvName)).setText(clickItem.getRecipientName());
//        if (Math.random() >= 0.5) {
//            ((TextView) holderView.findViewById(R.id.tvGender)).setText("女士");
//        } else {
//            ((TextView) holderView.findViewById(R.id.tvGender)).setText("先生");
//        }
        ((TextView) holderView.findViewById(R.id.tvGender)).setText(""); // no gender data
        ((TextView)holderView.findViewById(R.id.tvPhone)).setText(clickItem.getRecipientPhone());
        ((TextView)holderView.findViewById(R.id.tvAddress)).setText(clickItem.getAddress());
        ((TextView)holderView.findViewById(R.id.tvReservationTitle)).setText(R.string.failure_reason);
        ((TextView)holderView.findViewById(R.id.tvRes1)).setText(clickItem.getFailure());

        (holderView.findViewById(R.id.tvRes2)).setVisibility(View.INVISIBLE);
        (holderView.findViewById(R.id.textView)).setVisibility(View.INVISIBLE);

        dialog.show();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scheduleInfo = getSharedPreferences("schedule_info", MODE_PRIVATE);
        lastSchedule = scheduleInfo.getLong("last_schedule", 0);
//        init(); // this function will be called automatically after views initialized
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.plan_result_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.completed:
                Intent intent = new Intent(PlanResultActivity.this, CompletedOrderActivity_.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("orders", completedOrders);
//                CompletedOrderActivity_.intent(PlanResultActivity.this).completedOrders().start();
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                return true;
            case android.R.id.home:
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    // layout injection must allow time for view binding
    @AfterViews
    void init() {


        progressBar.setColorSchemeResources(R.color.colorPrimary);


        setupActionBar();
        // Reside Menu Initialization
        // attach to current activity;
        initResideMenu();
        registerMessageReceiver();

        orders = new ArrayList<>();
        readyOrders = new ArrayList<>();
        uninformedOrders = new ArrayList<>();
        failedOrders = new ArrayList<>();
        completedOrders = new ArrayList<>();
        currentCard = new ArrayList<>();

        users = new ArrayList<>();

        // Get a Request Queue
        queue = HttpClientRequest.getInstance(this.getApplicationContext()).getRequestQueue();


        CardView cardView = (CardView) findViewById(R.id.card_view);

        currentCard.add(tvCurrent);
        currentCard.add(tvAddress);
        currentCard.add(tvArriveTime);
        currentCard.add(tvEwt);
        currentCard.add(tvName);
        currentCard.add(tvOrderId);

        if (waitingList != null && uninformedList != null && failedList != null) {

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PlanResultActivity.this, MapActivity_.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("current", current);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                }
            });

            pullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Log.d("schedule", "refreshing list");
                    showProgress(true);
                    Toast.makeText(PlanResultActivity.this, "正在刷新...", Toast.LENGTH_SHORT).show();
                    GsonRequest gsonRequest = new GsonRequest.RequestBuilder()
//                    .post()
                            .url(Constants.BASE_URL)
                            .addParams("courier", InitApplication.courier_id)
                            .addParams("task", getDateString())
                            .addParams("lazy", "") // lazy mode, fetch order list without rescheduling
                            .clazz(OrderListGson.class)
                            .successListener(new Response.Listener() {
                                @Override
                                public void onResponse(Object response) {
                                    showProgress(false);
                                    orders.clear();
                                    readyOrders.clear();
                                    uninformedOrders.clear();
                                    completedOrders.clear();
                                    failedOrders.clear();
                                    orders.addAll(Arrays.asList(((OrderListGson) response).getOrders()));
                                    Collections.sort(orders, PlanResultActivity.this);
                                    for (Order order : orders) {
                                        switch (order.getState()) {
                                            case Constants.STATUS_READY:
                                                readyOrders.add(order);
//                                        Toast.makeText(PlanResultActivity.this, order.getAddress(), Toast.LENGTH_SHORT).show();
                                                break;
                                            case Constants.STATUS_UNINFORMED:
                                                uninformedOrders.add(order);
                                                break;
                                            case Constants.STATUS_COMPLETED:
                                                completedOrders.add(order);
                                                break;
                                            case Constants.STATUS_FAILED:
                                                failedOrders.add(order);
                                                break;
                                        }
                                    }

                                    refreshList();

                                    pullToRefreshView.setRefreshing(false);
                                    Toast.makeText(PlanResultActivity.this, "刷新完毕", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .errorListener(new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(PlanResultActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            })
                            .build();
                    queue.add(gsonRequest);
                }
            });

            schedule();
        }
    }

    private void initResideMenu() {
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        itemHome     = new ResideMenuItem(this, R.drawable.ic_menu_home,     "主页");
        itemToday    = new ResideMenuItem(this, android.R.drawable.ic_menu_today,  "今日配送");
        itemHistory  = new ResideMenuItem(this, android.R.drawable.ic_menu_recent_history, "配送历史");
        itemLogout   = new ResideMenuItem(this, android.R.drawable.ic_menu_close_clear_cancel, "登出");
        itemMyList   = new ResideMenuItem(this, android.R.drawable.ic_menu_my_calendar, "我的订单");

        itemHome.setOnClickListener(this);
        itemToday.setOnClickListener(this);
        itemHistory.setOnClickListener(this);
        itemLogout.setOnClickListener(this);
        itemMyList.setOnClickListener(this);

        loginInfo = getSharedPreferences("login", MODE_PRIVATE);
        isCourier = loginInfo.getBoolean("isCourier", false);

        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);

        if (isCourier) {
            resideMenu.addMenuItem(itemToday, ResideMenu.DIRECTION_LEFT);
            resideMenu.addMenuItem(itemHistory, ResideMenu.DIRECTION_LEFT);
        } else {
            resideMenu.addMenuItem(itemMyList, ResideMenu.DIRECTION_LEFT);
        }
        resideMenu.addMenuItem(itemLogout, ResideMenu.DIRECTION_LEFT);
    }


    private void schedule() {
        GsonRequest gsonRequest;
        if (lastSchedule + Constants.SCHEDULE_INTERVAL <= System.currentTimeMillis()) {
            Log.d("schedule", "normal mode");
            gsonRequest = new GsonRequest.RequestBuilder()
//                    .post()
                    .url(Constants.BASE_URL)
                    .addParams("courier", InitApplication.courier_id)
                    .addParams("task", getDateString())
                    .clazz(OrderListGson.class)
                    .successListener(new Response.Listener() {
                        @Override
                        public void onResponse(Object response) {
                            lastSchedule = System.currentTimeMillis();
                            SharedPreferences.Editor editor = scheduleInfo.edit();
                            editor.putLong("last_schedule", lastSchedule);
                            editor.apply();
                            showProgress(false);
                            orders.clear();
                            orders.addAll(Arrays.asList(((OrderListGson) response).getOrders()));
                            Collections.sort(orders, PlanResultActivity.this);
                            for (Order order : orders) {
                                switch (order.getState()) {
                                    case Constants.STATUS_READY:
                                        readyOrders.add(order);
//                                        Toast.makeText(PlanResultActivity.this, order.getAddress(), Toast.LENGTH_SHORT).show();
                                        break;
                                    case Constants.STATUS_UNINFORMED:
                                        uninformedOrders.add(order);
                                        break;
                                    case Constants.STATUS_COMPLETED:
                                        completedOrders.add(order);
                                        break;
                                    case Constants.STATUS_FAILED:
                                        failedOrders.add(order);
                                        break;
                                }
                            }
                            refreshList();
                        }
                    })
                    .errorListener(new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(PlanResultActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .build();
            queue.add(gsonRequest);
        } else {
            Log.d("schedule", "lazy mode");
            gsonRequest = new GsonRequest.RequestBuilder()
//                    .post()
                    .url(Constants.BASE_URL)
                    .addParams("courier", InitApplication.courier_id)
                    .addParams("task", getDateString())
                    .addParams("lazy", "") // lazy mode, fetch order list without rescheduling
                    .clazz(OrderListGson.class)
                    .successListener(new Response.Listener() {
                        @Override
                        public void onResponse(Object response) {
                            showProgress(false);
                            orders.clear();
                            orders.addAll(Arrays.asList(((OrderListGson) response).getOrders()));
                            Collections.sort(orders, PlanResultActivity.this);
                            for (Order order : orders) {
                                switch (order.getState()) {
                                    case Constants.STATUS_READY:
                                        readyOrders.add(order);
//                                        Toast.makeText(PlanResultActivity.this, order.getAddress(), Toast.LENGTH_SHORT).show();
                                        break;
                                    case Constants.STATUS_UNINFORMED:
                                        uninformedOrders.add(order);
                                        break;
                                    case Constants.STATUS_COMPLETED:
                                        completedOrders.add(order);
                                        break;
                                    case Constants.STATUS_FAILED:
                                        failedOrders.add(order);
                                        break;
                                }
                            }
                            refreshList();
                        }
                    })
                    .errorListener(new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(PlanResultActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .build();
            queue.add(gsonRequest);
        }
    }

    private void refreshList() {


        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

//        scrollView.setVisibility(View.GONE);
//        scrollView.animate().setDuration(shortAnimTime).alpha(0.5f).setListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                scrollView.setVisibility(View.GONE);
//            }
//        });

        scrollView.setVisibility(View.VISIBLE);
        scrollView.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                scrollView.setVisibility(View.VISIBLE);
            }
        });

        if (readyOrders.size() > 0) {
            current = readyOrders.get(0);
            tvCurrent.setText(getResources().getString(R.string.current_order));
            tvOrderId.setText(current.getOrderId());
//            if (Math.random()>=0.5) {
//                tvName.setText(current.getRecipientName() + " 女士 " + current.getRecipientPhone());
//            } else {
//                tvName.setText(current.getRecipientName() + " 先生 " + current.getRecipientPhone());
//            }

            Log.d("refresh", "arrive_time: " + current.getEstimation());
            tvName.setText(String.format("%s %s", current.getRecipientName(), current.getRecipientPhone()));
            tvAddress.setText(current.getAddress());
            tvArriveTime.setText(current.getEstimation());
            tvEwt.setText(String.format("%s 分钟", String.valueOf(current.getWaitTime())));



        } else {
            generateData();
            current = readyOrders.get(0);
            tvCurrent.setText(getResources().getString(R.string.current_order));
            tvOrderId.setText(current.getOrderId());
//            if (Math.random()>=0.5) {
//                tvName.setText(current.getRecipientName() + " 女士 " + current.getRecipientPhone());
//            } else {
//                tvName.setText(current.getRecipientName() + " 先生 " + current.getRecipientPhone());
//            }
            tvName.setText(String.format("%s%s", current.getRecipientName(), current.getRecipientPhone()));
            tvAddress.setText(current.getAddress());
            tvArriveTime.setText(current.getEstimation());
        }

//        if (readyOrders.size() == 1) {
//            Order emptyOrder = new Order("没", users.get(0), "啦.已通知订单全部配送完毕", 0, 0);
//            readyOrders.add(emptyOrder);
//        }
//        Toast.makeText(PlanResultActivity.this, String.valueOf(readyOrders.size()), Toast.LENGTH_SHORT).show();
        adapter_wait = new TodayAdapter(PlanResultActivity.this,
                R.layout.list_item_today, readyOrders.subList(1, readyOrders.size()));
        waitingList.setAdapter(adapter_wait);
        if (readyOrders.size() > 1) {
            tvWaiting.setVisibility(View.VISIBLE);
            setListViewHeightBasedOnItems(waitingList);
        } else {
            tvWaiting.setVisibility(View.INVISIBLE);
        }


        adapter_uninformed = new TodayAdapter(PlanResultActivity.this,
                R.layout.list_item_today, uninformedOrders);
        uninformedList.setAdapter(adapter_uninformed);
        if (uninformedOrders.size() > 0) {
            tvUninformed.setVisibility(View.VISIBLE);
            setListViewHeightBasedOnItems(uninformedList);
        } else {
            tvUninformed.setVisibility(View.INVISIBLE);
        }

        adapter_failure = new FailureAdapter(PlanResultActivity.this,
                R.layout.list_item_fail, failedOrders);
        failedList.setAdapter(adapter_failure);
        if (failedOrders.size() > 0) {
            tvFailed.setVisibility(View.VISIBLE);
            setListViewHeightBasedOnItems(failedList);
        } else {
            tvFailed.setVisibility(View.INVISIBLE);
        }

        // important - refresh views
        Log.d("refresh", "start refreshing views");
        Log.d("refresh", "Current Order: " + current.getOrderId());
        for (TextView tv : currentCard) {
            tv.postInvalidate();
        }
        adapter_wait.notifyDataSetChanged();
        adapter_uninformed.notifyDataSetChanged();
        adapter_failure.notifyDataSetChanged();
    }


    private void generateData() {

        users.clear();
        readyOrders.clear();

        users.add(new User("0", "魏奎", "15210832530", ""));
        users.add(new User("1", "乔子健", "18500323459", ""));
        users.add(new User("2", "辛超", "18810335615", ""));
        users.add(new User("3", "江天源", "15652240437", ""));
        users.add(new User("4", "温九", "13260486517", ""));
        users.add(new User("5", "孙志玉", "18810521016", ""));
        users.add(new User("6", "邵嘉伦", "18810521140", ""));

        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        Calendar calendare = Calendar.getInstance();
        calendar1.set(2015, Calendar.DECEMBER, 16, 9, 30);
        calendare.set(2015, Calendar.DECEMBER, 16, 9, 45);
        calendar2.set(2015, Calendar.DECEMBER, 16, 10, 0);

        String address = "北京市.北京大学理科一号楼1616";


        ArrayList<Failure> failures = new ArrayList<>();
        failures.add(new Failure("联系不上取件人"));
        failures.add(new Failure("取件人有急事"));

        for (int i = 0; i < 4; ++i) {
            Order order = new Order(String.valueOf(i), users.get((int)(Math.random()*6)), address,
                    calendar1.getTimeInMillis(), calendar2.getTimeInMillis());
            order.setFailure(failures.get((int) (Math.random() * 2)));
            order.setEstimation(calendare.getTimeInMillis());
            calendar1.add(Calendar.MINUTE, 15);
            calendar2.add(Calendar.MINUTE, 15);
            calendare.add(Calendar.MINUTE, 15);
            readyOrders.add(order);
        }
    }

    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ArrayAdapter listAdapter = (ArrayAdapter) listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }

    public static int getCurrentTimeInSecond() {
        Calendar calendar = Calendar.getInstance();
        return (calendar.get(Calendar.HOUR_OF_DAY) * 60
                + calendar.get(Calendar.MINUTE)) *60
                +calendar.get(Calendar.SECOND);
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    protected void onResume() {
        isForeground = true;
        super.onResume();
    }


    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }


    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "edu.sei.eecs.pku.hermes.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";
    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, filter);
    }

    @Override
    public int compare(Order lhs, Order rhs) {
        return (lhs.getArriveTime() - rhs.getArriveTime() >= 0) ? 1 : -1;
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            PlanResultActivity.this.finish();
        }
    }


    public static String getDateString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    @Override
    public void onClick(View v) {
        if (v == itemHome) {
            resideMenu.closeMenu();
            TodayActivity_.intent(PlanResultActivity.this).start();
            overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            finish();
        } else if (v == itemToday) {
            resideMenu.closeMenu();
        } else if (v == itemHistory) {
            resideMenu.closeMenu();
            CompletedOrderActivity_.intent(PlanResultActivity.this).start();
            overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            finish();
        } else if (v == itemMyList) {
            resideMenu.closeMenu();

        } else if (v == itemLogout) {
            resideMenu.closeMenu();
            SharedPreferences.Editor editor = loginInfo.edit();
            editor.putBoolean("isLogged", false);
            editor.putBoolean("isCourier", false);
            editor.putString("access_token", "");
            editor.putString("refresh_token", "");
            editor.putLong("expires_by", 0);
            editor.apply();
            LoginActivity_.intent(PlanResultActivity.this).start();
            overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
            finish();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

//            scrollView.setVisibility(show ? View.GONE : View.VISIBLE);
            scrollView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    scrollView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

//            llHeaderProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            llHeaderProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    llHeaderProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            llHeaderProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            scrollView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
