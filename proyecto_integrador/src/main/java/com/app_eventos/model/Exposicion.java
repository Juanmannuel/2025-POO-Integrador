package com.app_eventos.model;

import java.time.LocalDateTime;

import com.app_eventos.model.enums.TipoArte;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.TipoRol;

public class Exposicion extends Evento {

    private TipoArte tipoArte;

    public Exposicion(String nombre, LocalDateTime fechaInicio, LocalDateTime fechaFin, TipoArte tipoArte) {
        super(nombre, fechaInicio, fechaFin, TipoEvento.EXPOSICION);
        setTipoArte(tipoArte);
    }

    public Exposicion() {
        super();
        setTipoEvento(TipoEvento.EXPOSICION);
    }

    /** Asigna el curador (máximo 1 por exposición). */
    public void asignarCurador(Persona persona) {
        if (persona == null) throw new IllegalArgumentException("El curador no puede ser nulo.");
        if (contarPorRol(TipoRol.CURADOR) >= 1) {
            throw new IllegalStateException("Ya hay un curador asignado.");
        }
        agregarResponsable(persona, TipoRol.CURADOR);
    }

    @Override
    protected boolean rolPermitido(TipoRol rol) {
        return rol == TipoRol.ORGANIZADOR || rol == TipoRol.CURADOR;
    }

    public TipoArte getTipoArte() {
        return tipoArte;
    }

    public void setTipoArte(TipoArte tipoArte) {
        if (tipoArte == null) {
            throw new IllegalArgumentException("El tipo de arte no puede ser nulo.");
        }
        this.tipoArte = tipoArte;
    }
}
