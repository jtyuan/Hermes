package edu.sei.eecs.pku.hermes.utils.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.sei.eecs.pku.hermes.R;
import edu.sei.eecs.pku.hermes.model.Order;

/**
 * Created by bilibili on 15/11/8.
 */
public class TodayAdapter_recycler extends RecyclerView.Adapter<TodayAdapter_recycler.ViewHolder> {

    private Context context;
    private int resource;
    private ArrayList<Order> orders;

    private LayoutInflater inflater;

    public TodayAdapter_recycler(Context context, int resource, List<Order> objects) {
        this.context = context;
        this.resource = resource;
        this.inflater = LayoutInflater.from(context);
        this.orders = new ArrayList<>(objects);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(resource, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Order order = getItem(position);
        holder.setViews(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public Order getItem(int position) {
        return orders.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvOrderId;
        TextView tvRecipient;
        TextView tvPhoneNum;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tvOrderId = (TextView) itemView.findViewById(R.id.tvOrderId);
            this.tvPhoneNum = (TextView) itemView.findViewById(R.id.tvPhoneNum);
            this.tvRecipient = (TextView) itemView.findViewById(R.id.tvRecipient);
        }

        public void setViews(Order order) {
            String id = order.getOrderId();
            String name = order.getRecipientName();
            String phone = order.getRecipientPhone();
            this.tvOrderId.setText(id);
            this.tvRecipient.setText(name);
            this.tvPhoneNum.setText(phone);
        }

    }
}
