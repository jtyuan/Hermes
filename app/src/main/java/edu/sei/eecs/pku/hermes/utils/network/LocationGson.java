package edu.sei.eecs.pku.hermes.utils.network;

/**
 * Created by bilibili on 16/5/5.
 */
public class LocationGson {
    public String status;
    public Double lat;
    public Double lon;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }
}
