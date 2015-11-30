package edu.sei.eecs.pku.hermes.utils.network;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import edu.sei.eecs.pku.hermes.model.ConflictOrder;
import edu.sei.eecs.pku.hermes.model.Order;
import edu.sei.eecs.pku.hermes.model.ReadyOrder;

/**
 * Created by bilibili on 15/11/29.
 */
public class ScheduledListDeserializer implements JsonDeserializer<ScheduledListGson> {

    @Override
    public ScheduledListGson deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        final String status = jsonObject.get("status").getAsString();

        final ReadyOrder[] readyOrders = context.deserialize(jsonObject.get("already_schedule"), ReadyOrder[].class);
        final ConflictOrder[] conflictOrders = context.deserialize(jsonObject.get("conflict_orders"), ConflictOrder[].class);

        final ScheduledListGson list = new ScheduledListGson();
        list.setStatus(status);
        list.setReadyOrders(readyOrders);
        list.setConflictOrders(conflictOrders);

        return list;
    }
}