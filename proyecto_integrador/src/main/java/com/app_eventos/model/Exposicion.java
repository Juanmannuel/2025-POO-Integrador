package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.*;
import java.time.LocalDateTime;

/** Exposición con un curador como máximo. */
@Entity
@Table(name = "exposicion")
public class Exposicion extends Evento {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipoArte", nullable = false)
    private TipoArte tipoArte;

    public Exposicion() {
        super();
        setTipoEvento(TipoEvento.EXPOSICION);
    }

    public Exposicion(String n, LocalDateTime fi, LocalDateTime ff, TipoArte t) {
        super(n, fi, ff, TipoEvento.EXPOSICION);
        setTipoArte(t);
    }

    // Propio de Exposición 
    public TipoArte getTipoArte() { return tipoArte; }

    public void setTipoArte(TipoArte t) {
        if (t == null) throw new IllegalArgumentException("TipoArte nulo.");
        this.tipoArte = t;
    }

    /** Solo Organizador o Curador tienen sentido en Exposición. */
    @Override
    protected boolean rolPermitido(TipoRol rol) {
        return rol == TipoRol.ORGANIZADOR || rol == TipoRol.CURADOR;
    }

    /** máximo 1 curador */
    @Override
    protected void validarRestriccionesRol(TipoRol rol, Persona persona) {
        if (rol == TipoRol.CURADOR && contarPorRol(TipoRol.CURADOR) >= 1) {
            throw new IllegalStateException("La exposición solo admite un curador.");
        }
    }

    /** Asigna un curador. */
    public void asignarCurador(Persona persona) {
        if (persona == null) throw new IllegalArgumentException("Curador nulo.");
        agregarResponsable(persona, TipoRol.CURADOR); // pasa por validarRestriccionesRol(...)
    }
}
