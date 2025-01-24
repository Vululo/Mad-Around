package com.brunov.proyectointegrador;

public class Fuentes {



    private String ESTADO;
    private double LATITUD;
    private double LONGITUD;
    private String tipoVia;
    private String NOM_VIA;
    private String USO;


    // Getters y Setters
    public String getEstado() { return ESTADO; }
    public void setEstado(String estado) { this.ESTADO = estado; }

    public double getLatitud() { return LATITUD; }
    public void setLatitud(double latitud) { this.LATITUD = latitud; }

    public double getLongitud() { return LONGITUD; }
    public void setLongitud(double longitud) { this.LONGITUD = longitud; }

    public String getTipoVia() { return tipoVia; }
    public void setTipoVia(String tipoVia) { this.tipoVia = tipoVia; }

    public String getNomVia() { return NOM_VIA; }
    public void setNomVia(String nomVia) { this.NOM_VIA = nomVia; }

    public String getUso() { return USO; }
    public void setUso(String Uso) { this.USO = Uso; }

}
