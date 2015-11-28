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

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.Holder;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.sei.eecs.pku.hermes.model.Failure;
import edu.sei.eecs.pku.hermes.model.Order;
import edu.sei.eecs.pku.hermes.model.User;
import edu.sei.eecs.pku.hermes.utils.adapters.FailureAdapter;
import edu.sei.eecs.pku.hermes.utils.adapters.TodayAdapter;

@EActivity(R.layout.activity_plan_result)
public class PlanResultActivity extends AppCompatActivity {

    ArrayList<Order> orders;
    ArrayList<User> users;

    @ViewById(R.id.waitingList)
    ListView waitingList;

    @ViewById(R.id.uninformedList)
    ListView uninformedList;

    @ViewById(R.id.failedList)
    ListView failedList;

    @ViewById(R.id.tvCurrent)
    TextView tvCurrent;


    @Click
    void buttonDone() {
        // TODO
    }

    @Click
    void buttonFailed() {
        List<String> list = new ArrayList<>();

        list.add(PlanResultActivity.this.getString(R.string.failed_reason_1));
        list.add(PlanResultActivity.this.getString(R.string.failed_reason_2));
        list.add(PlanResultActivity.this.getString(R.string.failed_reason_3));
        list.add(PlanResultActivity.this.getString(R.string.failed_reason_4));
        list.add(PlanResultActivity.this.getString(R.string.failed_reason_5));

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
                CompletedOrderActivity_.intent(this).start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // layout injection must allow time for view binding
    @AfterViews
    void init() {

        orders = new ArrayList<>();
        users = new ArrayList<>();

        if (waitingList != null && uninformedList != null && failedList != null) {


            // TODO
            tvCurrent.setText(getResources().getString(R.string.current_order, " (VIP 5)"));

            generateData();
            TodayAdapter adapter1 = new TodayAdapter(PlanResultActivity.this, R.layout.list_item_today, orders.subList(0,3));
            waitingList.setAdapter(adapter1);
            TodayAdapter adapter2 = new TodayAdapter(PlanResultActivity.this, R.layout.list_item_today, orders.subList(3, 6));
            uninformedList.setAdapter(adapter2);
            FailureAdapter adapter3 = new FailureAdapter(PlanResultActivity.this, R.layout.list_item_today, orders.subList(6, 9));
            failedList.setAdapter(adapter3);

            setListViewHeightBasedOnItems(waitingList);
            setListViewHeightBasedOnItems(uninformedList);
            setListViewHeightBasedOnItems(failedList);
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

        String address = "北京市 海淀区 颐和园路5号 燕园街道 北京大学理科一号楼1616";


        ArrayList<Failure> failures = new ArrayList<>();
        failures.add(new Failure("联系不上取件人"));
        failures.add(new Failure("取件人有急事"));

        for (int i = 0; i < 10; ++i) {
            Order order = new Order(String.valueOf(i), users.get((int)(Math.random()*6)), address,
                    calendar1.getTimeInMillis(), calendar2.getTimeInMillis());
            order.setFailure(failures.get((int) (Math.random() * 2)));
            order.setEstimation(calendare.getTimeInMillis());
            calendar1.add(Calendar.MINUTE, 15);
            calendar2.add(Calendar.MINUTE, 15);
            calendare.add(Calendar.MINUTE, 15);
            orders.add(order);
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
