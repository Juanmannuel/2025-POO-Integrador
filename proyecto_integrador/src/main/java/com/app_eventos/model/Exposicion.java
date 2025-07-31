package com.app_eventos.model;

import java.time.Duration;
import java.time.LocalDateTime;

import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoArte;
import com.app_eventos.model.enums.TipoEvento;

public class Exposicion extends Evento {
    private TipoArte tipoArte;
    private Persona curador;

    // Constructor
    public Exposicion(Long idEvento, String nombre, String descripcion,
                      LocalDateTime fechaInicio, LocalDateTime fechaFin,
                      Duration duracionEstimada, EstadoEvento estado,
                      TipoArte tipoArte, Persona curador) {

        super(idEvento, TipoEvento.EXPOSICION, nombre, descripcion,
              fechaInicio, fechaFin, duracionEstimada, estado);

        this.tipoArte = tipoArte;
        this.curador = curador;
    }

    // Getters y Setters
    public TipoArte getTipoArte() {
        return tipoArte;
    }

    public void setTipoArte(TipoArte tipoArte) {
        this.tipoArte = tipoArte;
    }

    public Persona getCurador() {
        return curador;
    }

    public void setCurador(Persona curador) {
        this.curador = curador;
    }
}
