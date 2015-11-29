package edu.sei.eecs.pku.hermes.utils.network;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import edu.sei.eecs.pku.hermes.model.Order;
import edu.sei.eecs.pku.hermes.model.User;

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
        final String appointment = jsonObject.get("appointment").getAsString();
        final long reserveBegin = Order.parseAppointment(appointment, Order.BEGIN);
        final long reserveEnd = Order.parseAppointment(appointment, Order.END);
        final long signNeedTime = jsonObject.get("sign_need_time").getAsLong();
        final int state = jsonObject.get("status").getAsInt();
        final int rank = jsonObject.get("vip_level").getAsInt();

        final Order order = new Order(orderId, address, reserveBegin, reserveEnd);
        order.setRecipientVIPRank(rank);
        order.setCourierId(courierId);
        order.setSignNeedTime(signNeedTime);
        order.setState(state);
        return order;
    }
}