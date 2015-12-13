package edu.sei.eecs.pku.hermes.utils.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import edu.sei.eecs.pku.hermes.R;
import edu.sei.eecs.pku.hermes.model.Order;

/**
 * Created by bilibili on 15/11/8.
 */
public class FailureAdapter extends ArrayAdapter<Order> {

    private Context context;
    private int resource;
//    private ArrayList<Order> orders;

    private LayoutInflater inflater;

    public FailureAdapter(Context context, int resource, List<Order> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.inflater = LayoutInflater.from(context);
//        this.orders = new ArrayList<>(objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Order order = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(resource, null);
            holder = new ViewHolder();
            holder.tvOrderId = (TextView) convertView.findViewById(R.id.tvOrderId);
            holder.tvPhoneNum = (TextView) convertView.findViewById(R.id.tvPhoneNum);
            holder.tvRecipient = (TextView) convertView.findViewById(R.id.tvRecipient);
            holder.tvFailure = (TextView) convertView.findViewById(R.id.tvAddress);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.setViews(order);

        return convertView;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    class ViewHolder {

        TextView tvOrderId;
        TextView tvRecipient;
        TextView tvFailure;
        TextView tvPhoneNum;

        public void setViews(Order order) {
            String id = order.getOrderId();
            String name = order.getRecipientName();
            String phone = order.getRecipientPhone();
            String failure = order.getFailure();
            this.tvOrderId.setText(id);
            this.tvRecipient.setText(name);
            this.tvPhoneNum.setText(phone);
            this.tvFailure.setText(failure);
        }

    }
}
