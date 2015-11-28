package edu.sei.eecs.pku.hermes.model;

/**
 * Created by bilibili on 15/11/16.
 */
public class Failure {
    public int failureId;
    public String failureReason;

    public Failure(String failureReason) {
        this.failureReason = failureReason;
    }
}
