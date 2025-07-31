package com.app_eventos.modelo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.app_eventos.modelo.enums.EstadoEvento;
import com.app_eventos.modelo.enums.TipoEvento;

public class CicloCine extends Evento {
    private List<Pelicula> peliculas;
    private boolean postCharla;

    // Constructor
    public CicloCine(Long idEvento, String nombre, String descripcion,
                     LocalDateTime fechaInicio, LocalDateTime fechaFin,
                     Duration duracionEstimada, EstadoEvento estado,
                     boolean postCharla) {

        super(idEvento, TipoEvento.CICLO_CINE, nombre, descripcion,
              fechaInicio, fechaFin, duracionEstimada, estado);

        this.peliculas = new ArrayList<>();
        this.postCharla = postCharla;
    }

    // Getters y Setters
    public List<Pelicula> getPeliculas() {
        return peliculas;
    }

    public void setPeliculas(List<Pelicula> peliculas) {
        this.peliculas = peliculas;
    }

    public boolean isPostCharla() {
        return postCharla;
    }

    public void setPostCharla(boolean postCharla) {
        this.postCharla = postCharla;
    }
}
