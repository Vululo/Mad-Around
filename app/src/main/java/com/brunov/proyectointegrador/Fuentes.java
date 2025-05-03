package com.brunov.proyectointegrador;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Fuentes implements ClusterItem {

    private String BARRIO;
    private String ESTADO;
    private double LATITUD;
    private double LONGITUD;
    private String tipoVia;
    private String NOM_VIA;
    private String USO;
    private LatLng posicion;

    public String getBarrio() { return BARRIO; }
    public void setBarrio(String BARRIO) { this.BARRIO = BARRIO; }

    public String getEstado() { return ESTADO; }
    public void setEstado(String ESTADO) { this.ESTADO = ESTADO; }

    public double getLatitud() { return LATITUD; }
    public void setLatitud(double LATITUD) { this.LATITUD = LATITUD; }

    public double getLongitud() { return LONGITUD; }
    public void setLongitud(double LONGITUD) { this.LONGITUD = LONGITUD; }

    public String getNomVia() { return NOM_VIA; }
    public void setNomVia(String nomVia) { this.NOM_VIA = nomVia; }

    public String getUso() { return USO; }
    public void setUso(String Uso) { this.USO = Uso; }

    @NonNull
    @Override
    public LatLng getPosition() {
        this.posicion = new LatLng(this.LATITUD,this.LONGITUD);
        return posicion;
    }

    @Nullable
    @Override
    public String getTitle() {
        return "";
    }

    @Nullable
    @Override
    public String getSnippet() {
        return "";
    }
}
