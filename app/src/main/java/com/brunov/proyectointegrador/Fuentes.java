package com.brunov.proyectointegrador;

public class Fuentes {

    private String BARRIO;
    private String ESTADO;
    private double LATITUD;
    private double LONGITUD;
    private String tipoVia;
    private String NOM_VIA;
    private String USO;

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

}
