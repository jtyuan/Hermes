package edu.sei.eecs.pku.hermes.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by bilibili on 15/11/8.
 */
public class Order implements Serializable {

    private static final long serialVersionUID = -1185929572883854322L;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.CHINA);

    private String orderId;
    private User recipient;
    private String address;
    private long reserveBegin;
    private long reserveEnd;
    private long estimation;
    private Failure failure;

    private int state;

    public Order (String id, User user, String address) {
        this.orderId = id;
        this.recipient = user;
        this.address = address;
    }

    public Order (String id, User user, String address, long reserveBegin, long reserveEnd) {
        this.orderId = id;
        this.recipient = user;
        this.address = address;
        this.reserveBegin = reserveBegin;
        this.reserveEnd = reserveEnd;
    }

    public String getOrderId() {
        return orderId;
    }

    public User getRecipient() {
        return recipient;
    }

    public String getRecipientId() {
        return recipient.getUserId();
    }

    public String getRecipientName() {
        return recipient.getUserName();
    }

    public String getRecipientPhone() {
        return recipient.getUserPhoneNum();
    }

    public String getRecipientEmail() {
        return recipient.getUserEmail();
    }

    public long getReserveBeginLong() {
        return reserveBegin;
    }

    public long getReserveEndLong() {
        return reserveEnd;
    }

    public String getReserveBegin() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(reserveBegin);
        return sdf.format(calendar.getTime());
    }

    public String getReserveEnd() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(reserveEnd);
        return sdf.format(calendar.getTime());
    }

    public String getEstimation() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(estimation);
        return sdf.format(calendar.getTime());
    }

    public String getFailure() {
        return failure.failureReason;
    }

    public int getState() {
        return state;
    }

    public String getAddress() {
        return address;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public void SetRecipientId(String id) {
        recipient.setUserId(id);
    }

    public void setRecipientName(String name) {
        recipient.setUserName(name);
    }

    public void setRecipientPhone(String phone) {
        recipient.setUserPhoneNum(phone);
    }

    public void setRecipientEmail(String email) {
        recipient.setUserEmail(email);
    }

    public void setReserveBegin(long reserveBegin) {
        this.reserveBegin = reserveBegin;
    }

    public void setReserveEnd(long reserveEnd) {
        this.reserveEnd = reserveEnd;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setEstimation(long estimation) {
        this.estimation = estimation;
    }

    public void setFailure(Failure failure) {
        this.failure = failure;
    }



    public void setFailure(String failedReason) {
        this.failure.failureReason = failedReason;
    }

    public int getVIPRank() {
        return recipient.getUserVIPRank();
    }

    public void setState(int state) {
        this.state = state;
    }
}
