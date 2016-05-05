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
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
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
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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

@EActivity(R.layout.activity_user_list)
public class UserListActivity extends AppCompatActivity implements View.OnClickListener, Comparator<Order> {
    public static boolean isForeground = false;

    private SharedPreferences loginInfo;

    private RequestQueue queue;

    private ArrayList<Order> orders;
    private ArrayList<Order> sendingOrders;
    private ArrayList<Order> waitingOrders;
    private ArrayList<Order> failedOrders;

    ArrayList<User> users;

    @ViewById(R.id.waitingList)
    ListView waitingList;

    @ViewById(R.id.sendingList)
    ListView sendingList;

    @ViewById(R.id.failedList)
    ListView failedList;

    @ViewById(R.id.tvSending)
    TextView tvSending;

    @ViewById(R.id.tvWaiting)
    TextView tvWaiting;

    @ViewById(R.id.tvFailed)
    TextView tvFailed;

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
    private TodayAdapter adapter_sending;
    private TodayAdapter adapter_waiting;
    private FailureAdapter adapter_failed;
    private String phoneNumber;



    @ItemClick
    void sendingListItemClicked(final Order clickItem) {
        Holder holder = new ViewHolder(R.layout.list_detail);

        View headerView = View.inflate(UserListActivity.this, R.layout.detail_dialog_header, null);
        View footerView = View.inflate(UserListActivity.this, R.layout.detail_dialog_footer_two_buttons, null);

        ((TextView)headerView.findViewById(R.id.tvHeaderTitle)).setText(R.string.detail_header_title);
        ((TextView)headerView.findViewById(R.id.tvHeaderSub)).setText(getResources()
                .getString(R.string.vip_rank, clickItem.getVIPRank()));
        (footerView.findViewById(R.id.tvFooterTitle)).setVisibility(View.INVISIBLE);
        ((Button)footerView.findViewById(R.id.footer_confirm_button)).setText("追踪订单");

        DialogPlus dialog = DialogPlus.newDialog(UserListActivity.this)
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
                                Intent intent = new Intent(UserListActivity.this, UserMapActivity_.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("current", clickItem);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                                dialog.dismiss();
                                break;
                            case R.id.footer_close_button:
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
    void waitingListItemClicked(Order clickItem) {
        Holder holder = new ViewHolder(R.layout.list_detail_raw);

        View headerView = View.inflate(UserListActivity.this, R.layout.detail_dialog_header, null);
        View footerView = View.inflate(UserListActivity.this, R.layout.detail_dialog_footer_one_button, null);

        ((TextView)headerView.findViewById(R.id.tvHeaderTitle)).setText(R.string.detail_header_title);
        ((TextView)headerView.findViewById(R.id.tvHeaderSub)).setText(getResources()
                .getString(R.string.vip_rank, clickItem.getVIPRank()));
        (footerView.findViewById(R.id.tvFooterTitle)).setVisibility(View.INVISIBLE);

        DialogPlus dialog = DialogPlus.newDialog(UserListActivity.this)
                .setContentHolder(holder)
                .setHeader(headerView)
                .setFooter(footerView)
                .setGravity(Gravity.CENTER)
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

        View headerView = View.inflate(UserListActivity.this, R.layout.detail_dialog_header, null);
        View footerView = View.inflate(UserListActivity.this, R.layout.detail_dialog_footer_one_button, null);

        ((TextView)headerView.findViewById(R.id.tvHeaderTitle)).setText(R.string.detail_header_title);
        ((TextView)headerView.findViewById(R.id.tvHeaderSub)).setText(getResources()
                .getString(R.string.vip_rank, clickItem.getVIPRank()));
        (footerView.findViewById(R.id.tvFooterTitle)).setVisibility(View.INVISIBLE);

        DialogPlus dialog = DialogPlus.newDialog(UserListActivity.this)
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

        // Get a Request Queue
        queue = HttpClientRequest.getInstance(this.getApplicationContext()).getRequestQueue();

        loginInfo = getSharedPreferences("login", MODE_PRIVATE);
        phoneNumber = loginInfo.getString("phone_number", "");
//        init(); // this function will be called automatically after views initialized
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
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
        setupActionBar();

        // Reside Menu Initialization
        // attach to current activity;
        initResideMenu();
        registerMessageReceiver();

        orders = new ArrayList<>();
        sendingOrders = new ArrayList<>();
        waitingOrders = new ArrayList<>();
        failedOrders = new ArrayList<>();

        users = new ArrayList<>();

        pullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetch();
            }
        });

        fetch();

    }

    private void initResideMenu() {
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        itemHome = new ResideMenuItem(this, R.drawable.ic_menu_home, "主页");
        itemToday = new ResideMenuItem(this, android.R.drawable.ic_menu_today, "今日配送");
        itemHistory = new ResideMenuItem(this, android.R.drawable.ic_menu_recent_history, "配送历史");
        itemLogout = new ResideMenuItem(this, android.R.drawable.ic_menu_close_clear_cancel, "登出");
        itemMyList = new ResideMenuItem(this, android.R.drawable.ic_menu_my_calendar, "我的订单");

        itemHome.setOnClickListener(this);
        itemToday.setOnClickListener(this);
        itemHistory.setOnClickListener(this);
        itemLogout.setOnClickListener(this);
        itemMyList.setOnClickListener(this);

        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);

        if (isCourier) {
            resideMenu.addMenuItem(itemToday, ResideMenu.DIRECTION_LEFT);
            resideMenu.addMenuItem(itemHistory, ResideMenu.DIRECTION_LEFT);
        } else {
            resideMenu.addMenuItem(itemMyList, ResideMenu.DIRECTION_LEFT);
        }
        resideMenu.addMenuItem(itemLogout, ResideMenu.DIRECTION_LEFT);
    }


    private void fetch() {
        showProgress(true);
        Toast.makeText(UserListActivity.this, "正在获取列表...", Toast.LENGTH_SHORT).show();
        GsonRequest gsonRequest = new GsonRequest.RequestBuilder()
//                .post()
                .url(Constants.BASE_URL)
                .addParams("user", phoneNumber)
                .clazz(OrderListGson.class)
                .successListener(new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        orders.clear();
                        sendingOrders.clear();
                        failedOrders.clear();
                        waitingOrders.clear();
                        orders.addAll(Arrays.asList(((OrderListGson) response).getOrders()));
                        Collections.sort(orders, UserListActivity.this);
                        for (Order order : orders) {
                            switch (order.getState()) {
                                case Constants.STATUS_READY:
                                    sendingOrders.add(order);
                                    break;
                                case Constants.STATUS_UNINFORMED:
                                    waitingOrders.add(order);
                                    break;
                                case Constants.STATUS_FAILED:
                                    failedOrders.add(order);
                                    break;
                            }
                        }
                        refreshList();
                        showProgress(false);
                        pullToRefreshView.setRefreshing(false);
                        Toast.makeText(UserListActivity.this, "获取完毕", Toast.LENGTH_SHORT).show();
                    }
                })
                .errorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(UserListActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        showProgress(false);
                    }
                })
                .build();
        queue.add(gsonRequest);

    }

    private void refreshList() {

        adapter_sending = new TodayAdapter(UserListActivity.this,
                R.layout.list_item_today, sendingOrders);
        sendingList.setAdapter(adapter_sending);
        if (sendingOrders.size() > 0) {
            tvSending.setVisibility(View.VISIBLE);
            setListViewHeightBasedOnItems(sendingList);
        } else {
            tvSending.setVisibility(View.INVISIBLE);
        }


        adapter_waiting = new TodayAdapter(UserListActivity.this,
                R.layout.list_item_today, waitingOrders);
        waitingList.setAdapter(adapter_waiting);
        if (waitingOrders.size() > 0) {
            tvWaiting.setVisibility(View.VISIBLE);
            setListViewHeightBasedOnItems(waitingList);
        } else {
            tvWaiting.setVisibility(View.INVISIBLE);
        }

        adapter_failed = new FailureAdapter(UserListActivity.this,
                R.layout.list_item_fail, failedOrders);
        failedList.setAdapter(adapter_failed);
        if (failedOrders.size() > 0) {
            tvFailed.setVisibility(View.VISIBLE);
            setListViewHeightBasedOnItems(failedList);
        } else {
            tvFailed.setVisibility(View.INVISIBLE);
        }

        // important - refresh views
        Log.d("refresh", "start refreshing views");

        adapter_sending.notifyDataSetChanged();
        adapter_waiting.notifyDataSetChanged();
        adapter_failed.notifyDataSetChanged();
    }


    private void generateData() {

        users.clear();
        orders.clear();
        sendingOrders.clear();
        waitingOrders.clear();
        failedOrders.clear();

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
            Order order = new Order(String.valueOf(i), users.get((int) (Math.random() * 6)), address,
                    calendar1.getTimeInMillis(), calendar2.getTimeInMillis());
            order.setFailure(failures.get((int) (Math.random() * 2)));
            order.setEstimation(calendare.getTimeInMillis());
            calendar1.add(Calendar.MINUTE, 15);
            calendar2.add(Calendar.MINUTE, 15);
            calendare.add(Calendar.MINUTE, 15);
            sendingOrders.add(order);
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
                + calendar.get(Calendar.MINUTE)) * 60
                + calendar.get(Calendar.SECOND);
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
            UserListActivity.this.finish();
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
            TodayActivity_.intent(UserListActivity.this).start();
            finish();
            overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
        } else if (v == itemToday) {
            resideMenu.closeMenu();
        } else if (v == itemHistory) {
            resideMenu.closeMenu();
            CompletedOrderActivity_.intent(UserListActivity.this).start();
            finish();
            overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
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
            LocationService_.intent(getApplication()).stop();
            LoginActivity_.intent(UserListActivity.this).start();
            finish();
            overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
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
