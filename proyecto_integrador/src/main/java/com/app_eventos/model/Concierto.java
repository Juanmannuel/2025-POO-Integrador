package com.app_eventos.model;

import java.time.Duration;
import java.time.LocalDateTime;

import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEntrada;
import com.app_eventos.model.enums.TipoEvento;

public class Concierto extends Evento {
    private String artista;
    private TipoEntrada tipoEntrada;
    private int cantidadAsistentes;

    // Constructor
    public Concierto(Long idEvento, String nombre, String descripcion,
                     LocalDateTime fechaInicio, LocalDateTime fechaFin,
                     Duration duracionEstimada, EstadoEvento estado,
                     String artista, TipoEntrada tipoEntrada, int cantidadAsistentes) {

        super(idEvento, TipoEvento.CONCIERTO, nombre, descripcion,
              fechaInicio, fechaFin, duracionEstimada, estado);

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
