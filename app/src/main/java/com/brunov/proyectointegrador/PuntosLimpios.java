package com.brunov.proyectointegrador;

import com.google.gson.annotations.SerializedName;

public class PuntosLimpios {

    @SerializedName("title")
    public String title;

    @SerializedName("location")
    public Location location;

    public static class Location {
        public double latitude;
        public double longitude;
    }
}
