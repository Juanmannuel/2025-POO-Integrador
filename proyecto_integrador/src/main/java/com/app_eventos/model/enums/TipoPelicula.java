package com.app_eventos.model.enums;

public enum TipoPelicula {
    DOS_D("2D"),
    TRES_D("3D");

    private final String etiqueta;
    TipoPelicula(String etiqueta){ this.etiqueta = etiqueta; }
    public String getEtiqueta(){ return etiqueta; }
}
