package edu.sei.eecs.pku.hermes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.Holder;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;

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
import edu.sei.eecs.pku.hermes.model.ReadyOrder;
import edu.sei.eecs.pku.hermes.model.User;
import edu.sei.eecs.pku.hermes.utils.adapters.FailureAdapter;
import edu.sei.eecs.pku.hermes.utils.adapters.TodayAdapter;
import edu.sei.eecs.pku.hermes.utils.network.GsonRequest;
import edu.sei.eecs.pku.hermes.utils.network.HttpClientRequest;
import edu.sei.eecs.pku.hermes.utils.network.OrderListDeserializer;
import edu.sei.eecs.pku.hermes.utils.network.OrderListGson;
import edu.sei.eecs.pku.hermes.utils.network.ScheduledListGson;

@EActivity(R.layout.activity_plan_result)
public class PlanResultActivity extends AppCompatActivity {

    RequestQueue queue;

    ArrayList<Order> orders;
    ArrayList<Order> readyOrders;
    ArrayList<Order> uninformedOrders;
    ArrayList<Order> failedOrders;

    @Extra("orderExtra")
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

    @Click
    void buttonDone() {
        readyOrders.remove(0);
        refreshList();
    }

    @Click
    void buttonFailed() {
        List<String> list = new ArrayList<>();

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
                                dialog.dismiss();
                                break;
                            case R.id.footer_close_button:
                                dialog.dismiss();
                                break;
                        }
                    }
                })
                .create();

        ((Spinner)dialog.getHolderView().findViewById(R.id.spinner)).setAdapter(adapter);

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
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
                CompletedOrderActivity_.intent(PlanResultActivity.this).start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // layout injection must allow time for view binding
    @AfterViews
    void init() {

        orders = new ArrayList<>();
        readyOrders = new ArrayList<>();
        uninformedOrders = new ArrayList<>();
        failedOrders = new ArrayList<>();

        users = new ArrayList<>();

        // Get a Request Queue
        queue = HttpClientRequest.getInstance(this.getApplicationContext()).getRequestQueue();

        if (waitingList != null && uninformedList != null && failedList != null) {

            GsonRequest gsonRequest = new GsonRequest.RequestBuilder()
                    .post()
                    .url(Constants.SCHEDULE_URL + "runSchedule")
                    .addParams("courierID", "jty")
                    .addParams("dispatch_date", "20151111")
                    .clazz(OrderListGson.class)
                    .successListener(new Response.Listener() {
                        @Override
                        public void onResponse(Object response) {
//                            generateData(); // TODO: remove this
                            orders.clear();
                            orders.addAll(Arrays.asList(((OrderListGson) response).getOrders()));
                            orders.remove(0);
                            for (Order order : orders) {
                                switch (order.getState()) {
                                    case Constants.STATUS_READY:
                                        readyOrders.add(order);
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
        TodayAdapter adapter1 = new TodayAdapter(PlanResultActivity.this,
                R.layout.list_item_today, readyOrders.subList(1, readyOrders.size()));
        waitingList.setAdapter(adapter1);
        if (uninformedOrders.size() > 1) {
            tvWaiting.setVisibility(View.VISIBLE);
            setListViewHeightBasedOnItems(uninformedList);
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
                R.layout.list_item_today, failedOrders);
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
}
