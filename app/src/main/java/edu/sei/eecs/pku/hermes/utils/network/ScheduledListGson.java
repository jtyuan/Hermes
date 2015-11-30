package edu.sei.eecs.pku.hermes.utils.network;

import edu.sei.eecs.pku.hermes.model.ConflictOrder;
import edu.sei.eecs.pku.hermes.model.ReadyOrder;

/**
 * Created by bilibili on 15/11/29.
 */
public class ScheduledListGson {
    private ReadyOrder[] readyOrders;
    private ConflictOrder[] conflictOrders;
    private String status;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ReadyOrder[] getReadyOrders() {
        return readyOrders;
    }

    public void setReadyOrders(ReadyOrder[] readyOrders) {
        this.readyOrders = readyOrders;
    }

    public ConflictOrder[] getConflictOrders() {
        return conflictOrders;
    }

    public void setConflictOrders(ConflictOrder[] conflictOrders) {
        this.conflictOrders = conflictOrders;
    }
}
