package edu.sei.eecs.pku.hermes.utils.network;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;

import edu.sei.eecs.pku.hermes.model.Order;

/**
 * Created by bilibili on 15/11/29.
 */
public class OrderDeserializer implements JsonDeserializer<Order> {

    @Override
    public Order deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        final String orderId = jsonObject.get("orderID").getAsString();
        final String courierId = jsonObject.get("courierID").getAsString();
        final String address = jsonObject.get("address").getAsString();
        final String name = jsonObject.get("name").getAsString();
        final String phone = jsonObject.get("phone").getAsString();
        final String appointment = jsonObject.get("appointment").getAsString();
        final long reserveBegin = Order.parseAppointment(appointment, Order.BEGIN);
        final long reserveEnd = Order.parseAppointment(appointment, Order.END);
        final int signNeedTime = jsonObject.get("sign_need_time").getAsInt();
        final int state = jsonObject.get("status").getAsInt();
        final int rank = jsonObject.get("vip_level").getAsInt();

        final int arriveTime = jsonObject.get("arrive_time").getAsInt();
        final int waitTime = jsonObject.get("wait_time").getAsInt();
        final int leaveTime = jsonObject.get("leave_time").getAsInt();
        final int realTime = jsonObject.get("real_time").getAsInt();

        final String failure = jsonObject.get("failure_reason").getAsString();

        final Order order = new Order(orderId, address, reserveBegin, reserveEnd);
        order.setRecipientVIPRank(rank);
        order.setCourierId(courierId);
        order.setRecipientName(name);
        order.setRecipientPhone(phone);
        order.setSignNeedTime(signNeedTime);
        order.setState(state);
        order.setArriveTime(arriveTime);
        order.setWaitTime(waitTime);
        order.setLeaveTime(leaveTime);
        order.setRealTime(realTime);
        order.setFailure(failure);
        return order;
    }
}