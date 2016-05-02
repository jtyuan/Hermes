package edu.sei.eecs.pku.hermes;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.Holder;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Calendar;

import edu.sei.eecs.pku.hermes.model.Order;
import edu.sei.eecs.pku.hermes.model.User;
import edu.sei.eecs.pku.hermes.utils.adapters.TodayAdapter;

@EActivity(R.layout.activity_completed_order)
public class CompletedOrderActivity extends AppCompatActivity {

    ArrayList<Order> orders;
    ArrayList<User> users;

    @ViewById(R.id.completedList)
    ListView completedList;

    @ItemClick
    public void completedListItemClicked(Order clickItem) {

        Holder holder = new ViewHolder(R.layout.list_detail_raw);

        View headerView = View.inflate(CompletedOrderActivity.this, R.layout.detail_dialog_header, null);
        View footerView = View.inflate(CompletedOrderActivity.this, R.layout.detail_dialog_footer_one_button, null);

        ((TextView)headerView.findViewById(R.id.tvHeaderTitle)).setText(R.string.detail_header_title);
        ((TextView)headerView.findViewById(R.id.tvHeaderSub)).setText(getResources()
                .getString(R.string.vip_rank, clickItem.getVIPRank()));
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
        ((TextView)holderView.findViewById(R.id.tvGender)).setText("先生"); // TODO
        ((TextView)holderView.findViewById(R.id.tvPhone)).setText(clickItem.getRecipientPhone());
        ((TextView)holderView.findViewById(R.id.tvAddress)).setText(clickItem.getAddress());
        ((TextView)holderView.findViewById(R.id.tvReservationTitle)).setText(R.string.arrive_time);
        ((TextView)holderView.findViewById(R.id.tvRes1)).setText(clickItem.getReserveBegin()); // TODO
        (holderView.findViewById(R.id.tvRes2)).setVisibility(View.INVISIBLE);
        (holderView.findViewById(R.id.textView)).setVisibility(View.INVISIBLE);

        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        init();
    }

    @AfterViews
    @SuppressWarnings("unchecked")
    void init() {
//        orders = new ArrayList<>();
        users = new ArrayList<>();
        setupActionBar();
        if (completedList != null) {

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                orders = (ArrayList<Order>) bundle.get("orders");
            }
//            generateData();
            TodayAdapter adapter = new TodayAdapter(CompletedOrderActivity.this, R.layout.list_item_today, orders);
            completedList.setAdapter(adapter);

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
}
