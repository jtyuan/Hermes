package edu.sei.eecs.pku.hermes.utils.network;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import edu.sei.eecs.pku.hermes.model.Order;
import edu.sei.eecs.pku.hermes.model.ReadyOrder;

/**
 * Created by bilibili on 15/11/29.
 */
public class ReadyOrderDeserializer implements JsonDeserializer<ReadyOrder> {

    @Override
    public ReadyOrder deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();


        final long waitTime = jsonObject.get("wait_time").getAsLong();
        final long leaveTime = jsonObject.get("leave_time").getAsLong();
        final long arriveTime = jsonObject.get("arrive_time").getAsLong();

        final ReadyOrder order = new ReadyOrder();
        order.setWaitTime(waitTime);
        order.setArriveTime(arriveTime);
        order.setLeaveTime(leaveTime);


        final JsonElement id = jsonObject.get("orderID");
        final JsonElement idx = jsonObject.get("point_index");

        String orderId;
        int orderIdx = 0;

        if (id != null) {
            orderId = id.getAsString();
            order.setOrderID(orderId);
            order.setIndex(-1);
        }

        if (idx != null) {
            orderIdx = idx.getAsInt();
            order.setOrderID("-1");
            order.setIndex(orderIdx);
        }

        return order;
    }
}