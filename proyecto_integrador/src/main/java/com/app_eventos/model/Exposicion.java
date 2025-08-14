package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.*;
import java.time.LocalDateTime;

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

    // Getters y Setters
    public TipoArte getTipoArte() { return tipoArte; }

    public void setTipoArte(TipoArte t) {
        if (t == null) throw new IllegalArgumentException("TipoArte nulo.");
        this.tipoArte = t;
    }

    // Participantes
    @Override
    protected boolean rolPermitido(TipoRol rol) {
        return rol == TipoRol.ORGANIZADOR || rol == TipoRol.CURADOR;
    }

    // Validaciones de rol
    @Override
    protected void validarRestriccionesRol(TipoRol rol, Persona persona) {
        if (rol == TipoRol.CURADOR && contarPorRol(TipoRol.CURADOR) >= 1) {
            throw new IllegalStateException("La exposición solo admite un curador.");
        }
    }

    // Asignación de curador
    public void asignarCurador(Persona persona) {
        if (persona == null) throw new IllegalArgumentException("Curador nulo.");
        agregarResponsable(persona, TipoRol.CURADOR); // pasa por validarRestriccionesRol(...)
    }
}
