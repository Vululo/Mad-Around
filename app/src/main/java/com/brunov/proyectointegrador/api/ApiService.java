package com.brunov.proyectointegrador.api;

import retrofit2.http.GET;

import com.brunov.proyectointegrador.Bancos;
import com.brunov.proyectointegrador.Fuentes;
import com.brunov.proyectointegrador.PuntosLimpiosResponse;

import java.util.List;

import retrofit2.Call;

public interface ApiService {
    @GET("datosabiertos/CIUAB/MINT/FUENTES_BEBER/2025/01/fuentes202501.json")
    Call<List<Fuentes>> getFuentes();

    @GET("datosabiertos/CIUAB/MINT/BANCOS/2025/04/bancos202504.json")
    Call<List<Bancos>> getBancos();

    @GET("egob/catalogo/200284-0-puntos-limpios-fijos.json")
    Call<PuntosLimpiosResponse> getPuntosLimpios();
}