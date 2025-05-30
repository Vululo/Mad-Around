package com.brunov.proyectointegrador;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PuntosLimpiosResponse {

    @SerializedName("@graph")
    public List<PuntosLimpios> puntos;
}
