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

    // Getters / Setters
    public TipoArte getTipoArte() { return tipoArte; }

    public void setTipoArte(TipoArte t) {
        if (t == null) throw new IllegalArgumentException("Seleccione un tipo de arte.");
        this.tipoArte = t;
    }

    // Roles permitidos
    @Override
    protected boolean rolPermitido(TipoRol rol) {
        return rol == TipoRol.ORGANIZADOR || rol == TipoRol.CURADOR;
    }

    // Restricciones al asignar roles
    @Override
    protected void validarRestriccionesRol(TipoRol rol, Persona persona) {
        if (rol == TipoRol.CURADOR && contarPorRol(TipoRol.CURADOR) >= 1) {
            throw new IllegalStateException("La exposición solo admite un CURADOR.");
        }
    }

    // Invariantes del dominio
    @Override
    public void validarInvariantes() {
        super.validarInvariantes(); // exige al menos un ORGANIZADOR
        if (contarPorRol(TipoRol.CURADOR) < 1) {
            throw new IllegalStateException("La exposición debe tener un CURADOR.");
        }
    }
}