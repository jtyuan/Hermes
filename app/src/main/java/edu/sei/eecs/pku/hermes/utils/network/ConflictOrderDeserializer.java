package edu.sei.eecs.pku.hermes.utils.network;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import edu.sei.eecs.pku.hermes.model.ConflictOrder;

/**
 * Created by bilibili on 15/11/29.
 */
public class ConflictOrderDeserializer implements JsonDeserializer<ConflictOrder> {

    @Override
    public ConflictOrder deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        final ConflictOrder order = new ConflictOrder();

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