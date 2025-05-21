package com.brunov.proyectointegrador.api;

import retrofit2.http.GET;

import com.brunov.proyectointegrador.Fuentes;

import java.util.List;

import retrofit2.Call;

public interface ApiService {
    @GET("CIUAB/MINT/FUENTES_BEBER/2025/01/fuentes202501.json")
    Call<List<Fuentes>> getFuentes();
}