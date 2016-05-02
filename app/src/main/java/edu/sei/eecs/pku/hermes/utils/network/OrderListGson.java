package edu.sei.eecs.pku.hermes.utils.network;

import edu.sei.eecs.pku.hermes.model.Order;

/**
 * Created by bilibili on 15/11/29.
 */
public class OrderListGson {
    private String status;
    private Order [] orders;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Order[] getOrders() {
        return orders;
    }

    public void setOrders(Order[] orders) {
        this.orders = orders;
    }
}
