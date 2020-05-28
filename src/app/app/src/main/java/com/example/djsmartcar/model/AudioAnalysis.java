package com.example.djsmartcar.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AudioAnalysis {

    @SerializedName("meta")
    @Expose
    private Meta meta;
    @SerializedName("track")
    @Expose
    private Track track;

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }
}