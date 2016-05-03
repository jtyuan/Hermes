package edu.sei.eecs.pku.hermes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
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
        // TODO http request for real list
        GsonRequest gsonRequest = new GsonRequest.RequestBuilder()
//                .url(Constants.SCHEDULE_URL
//                        + "getOrders?courierID=" + inputCourierId.getText().toString().trim()
//                        + "&dispatch_date=" + "20151111")//TODO: should be sdf.format(Calendar.getInstance().getTime()))
                .url(Constants.BASE_URL)
                .addParams("courier", inputCourierId.getText().toString().trim())
                .addParams("task", "20151123")
                .clazz(OrderListGson.class)
                .successListener(new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        generateData(); // TODO: remove this
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
        ((TextView)holderView.findViewById(R.id.tvGender)).setText("先生"); // TODO
        ((TextView)holderView.findViewById(R.id.tvPhone)).setText(clickItem.getRecipientPhone());
        ((TextView)holderView.findViewById(R.id.tvAddress)).setText(clickItem.getAddress());
        ((TextView)holderView.findViewById(R.id.tvRes1)).setText(clickItem.getReserveBegin());
        ((TextView)holderView.findViewById(R.id.tvRes2)).setText(clickItem.getReserveEnd());

        // TODO: remove this when get real user data
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
    }

    @AfterViews
    public void init() {
        orders = new ArrayList<>();
        users = new ArrayList<>();

        // Get a Request Queue
        queue = HttpClientRequest.getInstance(this.getApplicationContext()).getRequestQueue();

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

        } else if (v == itemToday) {

        } else if (v == itemHistory) {

        } else if (v == itemMyList) {

        } else if (v == itemLogout) {
            SharedPreferences.Editor editor = loginInfo.edit();
            editor.putBoolean("isLogged", false);
            editor.putString("access_token", "");
            editor.putString("refresh_token", "");
            editor.putLong("expires_by", 0);
            editor.apply();
            LoginActivity_.intent(TodayActivity.this).start();
            overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
            finish();
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }
}
