package com.app_eventos.models;

import com.app_eventos.models.enums.TipoEntrada;

import java.time.LocalDate;

public class Concierto extends Evento {
    private String artista;
    private TipoEntrada tipoEntrada;
    private int cantidadAsistentes;

    // Constructor
    public Concierto(int codigo, String nombre, String descripcion, LocalDate fechaInicio, LocalDate fechaFin,
                     String artista, TipoEntrada tipoEntrada, int cantidadAsistentes) {
        super(codigo, nombre, descripcion, fechaInicio, fechaFin);
        this.artista = artista;
        this.tipoEntrada = tipoEntrada;
        this.cantidadAsistentes = cantidadAsistentes;
    }

    // Getters y Setters
    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public TipoEntrada getTipoEntrada() {
        return tipoEntrada;
    }

    public void setTipoEntrada(TipoEntrada tipoEntrada) {
        this.tipoEntrada = tipoEntrada;
    }

    public int getCantidadAsistentes() {
        return cantidadAsistentes;
    }

    public void setCantidadAsistentes(int cantidadAsistentes) {
        this.cantidadAsistentes = cantidadAsistentes;
    }
}
