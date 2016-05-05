package edu.sei.eecs.pku.hermes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.Locale;

import edu.sei.eecs.pku.hermes.configs.Constants;
import edu.sei.eecs.pku.hermes.model.Order;
import edu.sei.eecs.pku.hermes.model.User;
import edu.sei.eecs.pku.hermes.utils.adapters.TodayAdapter;
import edu.sei.eecs.pku.hermes.utils.network.GsonRequest;
import edu.sei.eecs.pku.hermes.utils.network.HttpClientRequest;
import edu.sei.eecs.pku.hermes.utils.network.OrderListGson;

@EActivity(R.layout.activity_completed_order)
public class CompletedOrderActivity extends AppCompatActivity implements View.OnClickListener {

    private RequestQueue queue;

    ArrayList<Order> orders;
    ArrayList<User> users;

    @ViewById(R.id.completedList)
    ListView completedList;

    @ViewById(R.id.llHeaderProgress)
    LinearLayout llHeaderProgress;

    @ViewById(R.id.progressBar)
    CircleProgressBar progressBar;

    @ViewById(R.id.pull_to_refresh)
    PullToRefreshView pullToRefreshView;

    @ViewById(R.id.cardToday)
    CardView cardView;

    private SharedPreferences loginInfo;
    private ResideMenu resideMenu;

    private boolean isCourier = false;
    private ResideMenuItem itemHome;
    private ResideMenuItem itemToday;
    private ResideMenuItem itemHistory;
    private ResideMenuItem itemLogout;
    private ResideMenuItem itemMyList;

    @ItemClick
    public void completedListItemClicked(Order clickItem) {

        Holder holder = new ViewHolder(R.layout.list_detail_raw);

        View headerView = View.inflate(CompletedOrderActivity.this, R.layout.detail_dialog_header, null);
        View footerView = View.inflate(CompletedOrderActivity.this, R.layout.detail_dialog_footer_one_button, null);

        ((TextView)headerView.findViewById(R.id.tvHeaderTitle)).setText(R.string.detail_header_title);
        ((TextView)headerView.findViewById(R.id.tvHeaderSub)).setText(getResources()
                .getString(R.string.completion_status,
                        clickItem.getState() == Constants.STATUS_COMPLETED ? "配送成功" : "配送失败"));
        (footerView.findViewById(R.id.tvFooterTitle)).setVisibility(View.INVISIBLE);

        DialogPlus dialog = DialogPlus.newDialog(CompletedOrderActivity.this)
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
        ((TextView)holderView.findViewById(R.id.tvReservationTitle)).setText(R.string.arrive_time);
        ((TextView)holderView.findViewById(R.id.tvRes1)).setText(clickItem.getFormatRealTime());

            (holderView.findViewById(R.id.tvRes2)).setVisibility(View.INVISIBLE);
            (holderView.findViewById(R.id.textView)).setVisibility(View.INVISIBLE);
        if (clickItem.getState() == Constants.STATUS_FAILED) {
            holderView.findViewById(R.id.tvFailedTitle).setVisibility(View.VISIBLE);
            holderView.findViewById(R.id.tvFailed).setVisibility(View.VISIBLE);
            ((TextView)holderView.findViewById(R.id.tvFailedTitle)).setText(R.string.failure_reason);
            ((TextView)holderView.findViewById(R.id.tvFailed)).setText(clickItem.getFailure());
        }

        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a Request Queue
        queue = HttpClientRequest.getInstance(this.getApplicationContext()).getRequestQueue();
    }

    @AfterViews
    @SuppressWarnings("unchecked")
    void init() {
//        orders = new ArrayList<>();
        initResideMenu();
        setupActionBar();

        users = new ArrayList<>();
        orders = new ArrayList<>();

        pullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("schedule", "refreshing list");
                Toast.makeText(CompletedOrderActivity.this, "正在刷新...", Toast.LENGTH_SHORT).show();
                refreshList();
            }
        });

        refreshList();
    }

    private void refreshList() {
        showProgress(true);
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
                        CompletedOrderActivity.this.orders.clear();
                        List<Order> orders = new ArrayList<>();
                        orders.addAll(Arrays.asList(((OrderListGson) response).getOrders()));
                        Collections.sort(orders, new Comparator<Order>() {
                            @Override
                            public int compare(Order lhs, Order rhs) {
                                return (int)(lhs.getRealTime() - rhs.getRealTime());
                            }
                        });
                        for (Order order : orders) {
                            switch (order.getState()) {
                                case Constants.STATUS_COMPLETED:
                                case Constants.STATUS_FAILED:
                                    CompletedOrderActivity.this.orders.add(order);
                                    break;
                            }
                        }

                        TodayAdapter adapter = new TodayAdapter(CompletedOrderActivity.this,
                                R.layout.list_item_today,
                                CompletedOrderActivity.this.orders);
                        completedList.setAdapter(adapter);
                        showProgress(false);
                        pullToRefreshView.setRefreshing(false);

                        if (CompletedOrderActivity.this.orders.size() == 0) {
                            Toast.makeText(CompletedOrderActivity.this, "还没有完成的订单哦.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CompletedOrderActivity.this, "获取完毕", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .errorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CompletedOrderActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .build();
        queue.add(gsonRequest);

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

    private void generateData() {

        users.clear();
        orders.clear();

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

        for (int i = 0; i < 10; ++i) {
            Order order = new Order(String.valueOf(i), users.get((int)(Math.random()*6)), address,
                    calendar1.getTimeInMillis(), calendar2.getTimeInMillis());
            order.setEstimation(calendare.getTimeInMillis());
            calendar1.add(Calendar.MINUTE, 15);
            calendar2.add(Calendar.MINUTE, 15);
            calendare.add(Calendar.MINUTE, 15);
            orders.add(order);
        }
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onClick(View v) {
        if (v == itemHome) {
            resideMenu.closeMenu();
            TodayActivity_.intent(CompletedOrderActivity.this).start();
            finish();
            overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
        } else if (v == itemToday) {
            resideMenu.closeMenu();
            PlanResultActivity_.intent(CompletedOrderActivity.this).start();
            finish();
            overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
        } else if (v == itemHistory) {
            resideMenu.closeMenu();
        } else if (v == itemMyList) {
            resideMenu.closeMenu();
            UserListActivity_.intent(CompletedOrderActivity.this).start();
            finish();
            overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
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
            LoginActivity_.intent(CompletedOrderActivity.this).start();
            finish();
            overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            cardView.setVisibility(show ? View.GONE : View.VISIBLE);
            cardView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    cardView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            llHeaderProgress.setVisibility(show ? View.VISIBLE : View.GONE);
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
            cardView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    public static String getDateString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                break;
        }
        return true;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

}
