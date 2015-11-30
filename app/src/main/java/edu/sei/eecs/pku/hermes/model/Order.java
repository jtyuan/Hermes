package edu.sei.eecs.pku.hermes.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by bilibili on 15/11/8.
 */
public class Order implements Serializable, Comparable {

    private static final long serialVersionUID = 3998467362700630317L;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.CHINA);
    public static final int BEGIN = 0;
    public static final int END = 1;

    private String orderId;
    private String courierId;

    private User recipient;
    private String address;
    private long reserveBegin;
    private long reserveEnd;
    private long estimation;

    private long signNeedTime;
    private long arriveTime;
    private long waitTime;
    private long leaveTime;

    private Failure failure;

    private int state;

    public Order (String id, User user, String address) {
        this.orderId = id;
        this.recipient = user;
        this.address = address;
    }

    public Order (String id, User user, String address, long reserveBegin, long reserveEnd) {
        this(id, user, address);
        this.reserveBegin = reserveBegin;
        this.reserveEnd = reserveEnd;
    }

    public Order (String id, String address) {
        this(id, new User("0", "姓名", "电话", "邮箱"), address);
    }

    public Order (String id, String address, long reserveBegin, long reserveEnd) {
        this(id, new User("0", "姓名", "电话", "邮箱"), address, reserveBegin, reserveEnd);
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

    public int getRecipientVIPRank() {
        return recipient.getUserVIPRank();
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

    public void setRecipientVIPRank(int rank) {
        recipient.setUserVIPRank(rank);
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



    public String getCourierId() {
        return courierId;
    }

    public void setCourierId(String courierId) {
        this.courierId = courierId;
    }

    public void setFailure(String failedReason) {
        if (this.failure == null)
            this.failure = new Failure(failedReason);
        else
            this.failure.failureReason = failedReason;
    }

    public int getVIPRank() {
        return recipient.getUserVIPRank();
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getSignNeedTime() {
        return signNeedTime;
    }

    public void setSignNeedTime(long signNeedTime) {
        this.signNeedTime = signNeedTime;
    }

    public static long parseAppointment(String appointment, int part) {
        int minutes = Integer.valueOf(appointment.split(",")[part]);
        int hour = minutes / 60;
        int minute = minutes % 60;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return calendar.getTimeInMillis();
    }

    public long getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(int arriveTime) {
        this.arriveTime = arriveTime;

        int hour = arriveTime / 60;
        int minute = arriveTime % 60;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        this.estimation = calendar.getTimeInMillis();
    }

    public long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public long getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(int leaveTime) {
        this.leaveTime = leaveTime;
    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }
}
