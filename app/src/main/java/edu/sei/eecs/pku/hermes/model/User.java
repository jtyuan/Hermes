package edu.sei.eecs.pku.hermes.model;

import java.io.Serializable;

/**
 * Created by bilibili on 15/11/8.
 */
public class User implements Serializable {

    private static final long serialVersionUID = -3705128460786869258L;
    private String userId;
    private String userName;
    private String userPhoneNum;
    private String userEmail;
    private String userGender;
    private int userVIPRank;

    public User(String id, String name, String phone, String email) {
        this.userId = id;
        this.userName = name;
        this.userPhoneNum = phone;
        this.userEmail = email;
        this.userGender = "女士";
        this.userVIPRank = 0;
    }

    public User(String id, String name, String gender, String phone, String email) {
        this.userId = id;
        this.userName = name;
        this.userPhoneNum = phone;
        this.userEmail = email;
        this.userGender = gender;
        this.userVIPRank = 0;
    }

    public int getUserVIPRank() {
        return userVIPRank;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPhoneNum() {
        return userPhoneNum;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPhoneNum(String userPhoneNum) {
        this.userPhoneNum = userPhoneNum;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public void setUserVIPRank(int userVIPRank) {
        this.userVIPRank = userVIPRank;
    }
}
