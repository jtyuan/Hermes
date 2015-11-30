package edu.sei.eecs.pku.hermes.utils.network;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import edu.sei.eecs.pku.hermes.model.Order;

/**
 * Created by bilibili on 15/11/29.
 */
public class OrderListDeserializer implements JsonDeserializer<OrderListGson> {

    @Override
    public OrderListGson deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        final String status = jsonObject.get("status").getAsString();

        final Order[] orders = context.deserialize(jsonObject.get("orders"), Order[].class);

        final OrderListGson list = new OrderListGson();
        list.setStatus(status);
        list.setOrders(orders);
        return list;
    }
}