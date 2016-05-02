package edu.sei.eecs.pku.hermes;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

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
public class PlanResultActivity extends AppCompatActivity {
    public static boolean isForeground = false;

    RequestQueue queue;

    ArrayList<Order> orders;
    ArrayList<Order> readyOrders;
    ArrayList<Order> uninformedOrders;
    ArrayList<Order> failedOrders;

    @Extra
    ArrayList<Order> completedOrders;

    ArrayList<User> users;

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

    @Click
    void buttonDone() {

        GsonRequest gsonRequest = new GsonRequest.RequestBuilder()
                .post()
                .url(Constants.BASE_URL + "/delivery/" + readyOrders.get(0).getOrderId())
                .addParams("real_time", String.valueOf(getCurrentTimeInSecond()))
                .addParams("type", "0")
                .addParams("message", "success")
                .clazz(ResultGson.class)
                .successListener(new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
//                            generateData(); // TODO: remove this
                        if (((ResultGson)response).status.equals("ok")) {
                            Log.d("response", ((ResultGson)response).status);
                            completedOrders.add(readyOrders.get(0));
                            readyOrders.remove(0);
                            refreshList();
                        }
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

    @Click
    void buttonFailed() {
        final List<String> list = new ArrayList<>();

        list.add(PlanResultActivity.this.getString(R.string.failed_reason_1));
        list.add(PlanResultActivity.this.getString(R.string.failed_reason_2));
        list.add(PlanResultActivity.this.getString(R.string.failed_reason_3));
//        list.add(PlanResultActivity.this.getString(R.string.failed_reason_4));
//        list.add(PlanResultActivity.this.getString(R.string.failed_reason_5));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(PlanResultActivity.this,
                android.R.layout.simple_spinner_dropdown_item, list);

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

                                GsonRequest gsonRequest = new GsonRequest.RequestBuilder()
                                        .post()
                                        .url(Constants.BASE_URL + "/delivery/" + readyOrders.get(0).getOrderId())
                                        .addParams("real_time", String.valueOf(getCurrentTimeInSecond()))
                                        .addParams("type", "1")
                                        .addParams("message", tvFailure.getText().toString())
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
        ((TextView)holderView.findViewById(R.id.tvGender)).setText("先生"); // TODO
        ((TextView)holderView.findViewById(R.id.tvPhone)).setText(clickItem.getRecipientPhone());
        ((TextView)holderView.findViewById(R.id.tvAddress)).setText(clickItem.getAddress());
        ((TextView)holderView.findViewById(R.id.tvRes1)).setText(clickItem.getReserveBegin());
        ((TextView)holderView.findViewById(R.id.tvRes2)).setText(clickItem.getReserveEnd());

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
        ((TextView)holderView.findViewById(R.id.tvGender)).setText("先生"); // TODO
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
        ((TextView)holderView.findViewById(R.id.tvGender)).setText("先生"); // TODO
        ((TextView)holderView.findViewById(R.id.tvPhone)).setText(clickItem.getRecipientPhone());
        ((TextView)holderView.findViewById(R.id.tvAddress)).setText(clickItem.getAddress());
        ((TextView)holderView.findViewById(R.id.tvReservationTitle)).setText("失败原因：");
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
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    // layout injection must allow time for view binding
    @AfterViews
    void init() {


        // Reside Menu Initialization
        // attach to current activity;
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        // create menu items;
        String titles[] = { "Home", "Profile", "Calendar", "Settings" };
        int icon[] = { android.R.drawable.ic_menu_agenda, android.R.drawable.ic_menu_zoom, android.R.drawable.ic_menu_agenda, android.R.drawable.ic_menu_agenda };

        for (int i = 0; i < titles.length; i++){
            ResideMenuItem item = new ResideMenuItem(this, icon[i], titles[i]);
//            item.setOnClickListener(this);
            resideMenu.addMenuItem(item,  ResideMenu.DIRECTION_LEFT); // or  ResideMenu.DIRECTION_RIGHT
        }


        orders = new ArrayList<>();
        readyOrders = new ArrayList<>();
        uninformedOrders = new ArrayList<>();
        failedOrders = new ArrayList<>();
        completedOrders = new ArrayList<>();

        users = new ArrayList<>();

        // Get a Request Queue
        queue = HttpClientRequest.getInstance(this.getApplicationContext()).getRequestQueue();

        registerMessageReceiver();

        CardView cardView = (CardView) findViewById(R.id.card_view);

//        setupActionBar();

        if (waitingList != null && uninformedList != null && failedList != null) {

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MapActivity_.intent(PlanResultActivity.this).start();
                    overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                }
            });

            pullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Toast.makeText(PlanResultActivity.this, "pulled!", Toast.LENGTH_SHORT).show();
                    pullToRefreshView.setRefreshing(false);
                }
            });


            progressBar.setColorSchemeResources(R.color.colorPrimary);

            GsonRequest gsonRequest = new GsonRequest.RequestBuilder()
//                    .post()
                    .url(Constants.BASE_URL)
                    .addParams("courier", "25430") // TODO: courierID
                    .addParams("task", "20151123")
                    .clazz(OrderListGson.class)
                    .successListener(new Response.Listener() {
                        @Override
                        public void onResponse(Object response) {
//                            generateData(); // TODO: remove this
                            Log.d("GsonRequest", "喵喵喵?");
                            llHeaderProgress.setVisibility(View.GONE);
                            scrollView.setVisibility(View.VISIBLE);
                            orders.clear();
                            orders.addAll(Arrays.asList(((OrderListGson) response).getOrders()));
                            orders.remove(0);
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

        if (readyOrders.size() > 0) {
            current = readyOrders.get(0);
            tvCurrent.setText(getResources().getString(R.string.current_order,
                    current.getVIPRank()));
            tvOrderId.setText(current.getOrderId());
            tvName.setText(current.getRecipientName() + " 先生 " + current.getRecipientPhone());
            tvAddress.setText(current.getAddress());
            tvArriveTime.setText(current.getEstimation());
            tvEwt.setText(String.valueOf(current.getWaitTime()) + " 分钟");
        } else {
            generateData();
            current = readyOrders.get(0);
            tvCurrent.setText(getResources().getString(R.string.current_order,
                    current.getVIPRank()));
            tvOrderId.setText(current.getOrderId());
            tvName.setText(current.getRecipientName() + " 先生 " + current.getRecipientPhone());
            tvAddress.setText(current.getAddress());
            tvArriveTime.setText(current.getEstimation());
        }

//        if (readyOrders.size() == 1) {
//            Order emptyOrder = new Order("没", users.get(0), "啦.已通知订单全部配送完毕", 0, 0);
//            readyOrders.add(emptyOrder);
//        }
//        Toast.makeText(PlanResultActivity.this, String.valueOf(readyOrders.size()), Toast.LENGTH_SHORT).show();
        TodayAdapter adapter1 = new TodayAdapter(PlanResultActivity.this,
                R.layout.list_item_today, readyOrders.subList(1, readyOrders.size()));
        waitingList.setAdapter(adapter1);
        if (readyOrders.size() > 1) {
            tvWaiting.setVisibility(View.VISIBLE);
            setListViewHeightBasedOnItems(waitingList);
        } else {
            tvWaiting.setVisibility(View.INVISIBLE);
        }

        TodayAdapter adapter2 = new TodayAdapter(PlanResultActivity.this,
                R.layout.list_item_today, uninformedOrders);
        uninformedList.setAdapter(adapter2);
        if (uninformedOrders.size() > 0) {
            tvUninformed.setVisibility(View.VISIBLE);
            setListViewHeightBasedOnItems(uninformedList);
        } else {
            tvUninformed.setVisibility(View.INVISIBLE);
        }

        FailureAdapter adapter3 = new FailureAdapter(PlanResultActivity.this,
                R.layout.list_item_fail, failedOrders);
        failedList.setAdapter(adapter3);
        if (failedOrders.size() > 0) {
            tvFailed.setVisibility(View.VISIBLE);
            setListViewHeightBasedOnItems(failedList);
        } else {
            tvFailed.setVisibility(View.INVISIBLE);
        }
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

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            PlanResultActivity.this.finish();
        }
    }


}
