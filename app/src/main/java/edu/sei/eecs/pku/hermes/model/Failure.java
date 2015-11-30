package edu.sei.eecs.pku.hermes.model;

import java.io.Serializable;

/**
 * Created by bilibili on 15/11/16.
 */
public class Failure implements Serializable {
    private static final long serialVersionUID = -5568488100703427001L;
    public int failureId;
    public String failureReason;

    public Failure(String failureReason) {
        this.failureReason = failureReason;
    }
}
