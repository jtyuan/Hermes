package edu.sei.eecs.pku.hermes;

import android.annotation.TargetApi;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.Holder;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import edu.sei.eecs.pku.hermes.configs.Constants;
import edu.sei.eecs.pku.hermes.model.Order;
import edu.sei.eecs.pku.hermes.model.User;
import edu.sei.eecs.pku.hermes.utils.adapters.TodayAdapter;
import edu.sei.eecs.pku.hermes.utils.network.GsonRequest;
import edu.sei.eecs.pku.hermes.utils.network.HttpClientRequest;
import edu.sei.eecs.pku.hermes.utils.network.OrderListGson;


@EActivity(R.layout.activity_today)
public class TodayActivity extends AppCompatActivity implements View.OnClickListener {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);

    RequestQueue queue;

    ArrayList<Order> orders;
    ArrayList<User> users;

    @ViewById(R.id.inputCourierId)
    EditText inputCourierId;

    @ViewById(R.id.todayList)
    ListView todayList;

    @ViewById(R.id.cardToday)
    CardView cardToday;

    private SharedPreferences loginInfo;
    private ResideMenu resideMenu;

    private boolean isCourier = false;
    private ResideMenuItem itemHome;
    private ResideMenuItem itemToday;
    private ResideMenuItem itemHistory;
    private ResideMenuItem itemLogout;
    private ResideMenuItem itemMyList;

    @Click
    void buttonConfirm() {
        GsonRequest gsonRequest = new GsonRequest.RequestBuilder()
                .url(Constants.BASE_URL)
                .addParams("courier", inputCourierId.getText().toString().trim())
                .addParams("task", "20151123")
                .clazz(OrderListGson.class)
                .successListener(new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        orders.clear();
                        orders.addAll(Arrays.asList(((OrderListGson)response).getOrders()));

                        // remove dispatching center from order list
                        final ArrayList<Order> removeList = new ArrayList<Order>();
                        for (Order order : orders) {
                            if (order.getState() == Constants.DISPATCHING_CENTER)
                                removeList.add(order);
                        }
                        orders.removeAll(removeList);

                        cardToday.setVisibility(View.VISIBLE);
                        TodayAdapter adapter = new TodayAdapter(TodayActivity.this, R.layout.list_item_today, orders);
                        todayList.setAdapter(adapter);
                    }
                })
                .errorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(TodayActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .build();
        queue.add(gsonRequest);
    }

    @Click
    void buttonPlan() {
        PlanResultActivity_.intent(TodayActivity.this).start();
        finish();
    }

    @ItemClick
    public void todayListItemClicked(Order clickItem) {
        Holder holder = new ViewHolder(R.layout.list_detail_raw);

        View headerView = View.inflate(TodayActivity.this, R.layout.detail_dialog_header, null);
        View footerView = View.inflate(TodayActivity.this, R.layout.detail_dialog_footer_one_button, null);

        ((TextView)headerView.findViewById(R.id.tvHeaderTitle)).setText(R.string.detail_header_title);
        ((TextView)headerView.findViewById(R.id.tvHeaderSub)).setText(getResources()
                .getString(R.string.vip_rank, clickItem.getVIPRank()));
        (footerView.findViewById(R.id.tvFooterTitle)).setVisibility(View.INVISIBLE);

        DialogPlus dialog = DialogPlus.newDialog(TodayActivity.this)
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
        if (Math.random() >= 0.5) {
            ((TextView) holderView.findViewById(R.id.tvGender)).setText("女士");
        } else {
            ((TextView) holderView.findViewById(R.id.tvGender)).setText("先生");
        }
        ((TextView)holderView.findViewById(R.id.tvPhone)).setText(clickItem.getRecipientPhone());
        ((TextView)holderView.findViewById(R.id.tvAddress)).setText(clickItem.getAddress());
        ((TextView)holderView.findViewById(R.id.tvRes1)).setText(clickItem.getReserveBegin());
        ((TextView)holderView.findViewById(R.id.tvRes2)).setText(clickItem.getReserveEnd());

        if (clickItem.getRecipientName().equals("姓名")) {
            User user = users.get((int) (Math.random() * 7));
            ((TextView)holderView.findViewById(R.id.tvName)).setText(user.getUserName());
            ((TextView)holderView.findViewById(R.id.tvPhone)).setText(user.getUserPhoneNum());
        }

        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        init();

        loginInfo = getSharedPreferences("login", MODE_PRIVATE);
        isCourier = loginInfo.getBoolean("isCourier", false);
        if (isCourier) {
//            this.startService(new Intent(this, LocationService.class));
            LocationService_.intent(getApplication()).start();
            PlanResultActivity_.intent(TodayActivity.this).start();
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else {
            UserListActivity_.intent(TodayActivity.this).start();
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }

        // TODO no idea what to put in this activity.. maybe nothing
    }

    @AfterViews
    public void init() {
        orders = new ArrayList<>();
        users = new ArrayList<>();

        setupActionBar();

        // Get a Request Queue
        queue = HttpClientRequest.getInstance(this.getApplicationContext()).getRequestQueue();

        setupResideMenu();

    }

    private void setupResideMenu() {
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

        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);

        if (isCourier) {
            resideMenu.addMenuItem(itemToday, ResideMenu.DIRECTION_LEFT);
            resideMenu.addMenuItem(itemHistory, ResideMenu.DIRECTION_LEFT);
        } else {
            resideMenu.addMenuItem(itemMyList, ResideMenu.DIRECTION_LEFT);
        }
        resideMenu.addMenuItem(itemLogout, ResideMenu.DIRECTION_LEFT);
    }

    private void generateData() {

        users.clear();
//        orders.clear();

        users.add(new User("0", "魏奎", "15210832530", ""));
        users.add(new User("1", "乔子健", "18500323459", ""));
        users.add(new User("2", "辛超", "18810335615", ""));
        users.add(new User("3", "江天源", "15652240437", ""));
        users.add(new User("4", "温九", "13260486517", ""));
        users.add(new User("5", "孙志玉", "18810521016", ""));
        users.add(new User("6", "邵嘉伦", "18810521140", ""));

//        Calendar calendar1 = Calendar.getInstance();
//        Calendar calendar2 = Calendar.getInstance();
//        calendar1.set(2015, Calendar.DECEMBER, 16, 9, 30);
//        calendar2.set(2015, Calendar.DECEMBER, 16, 10, 0);
//
//        String address = "北京市 海淀区 颐和园路5号 燕园街道 北京大学理科一号楼1616";

//        for (int i = 0; i < 10; ++i) {
//            Order order = new Order(String.valueOf(i), users.get((int)(Math.random()*7)), address,
//                    calendar1.getTimeInMillis(), calendar2.getTimeInMillis());
//            calendar1.add(Calendar.MINUTE, 15);
//            calendar2.add(Calendar.MINUTE, 15);
//            orders.add(order);
//        }
    }

    @Override
    public void onClick(View v) {
        if (v == itemHome) {
            resideMenu.closeMenu();
        } else if (v == itemToday) {
            resideMenu.closeMenu();
            PlanResultActivity_.intent(TodayActivity.this).start();
            finish();
            overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
        } else if (v == itemHistory) {
            resideMenu.closeMenu();
            CompletedOrderActivity_.intent(TodayActivity.this).start();
            finish();
            overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
        } else if (v == itemMyList) {
            resideMenu.closeMenu();
            UserListActivity_.intent(TodayActivity.this).start();
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
            LoginActivity_.intent(TodayActivity.this).start();
            finish();
            overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
        }
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

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
