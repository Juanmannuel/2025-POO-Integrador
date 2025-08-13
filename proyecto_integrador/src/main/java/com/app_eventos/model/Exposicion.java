package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.*;
import java.time.LocalDateTime;

/** Exposición con un curador como máximo. */
@Entity @Table(name = "exposicion")
public class Exposicion extends Evento {

    @Enumerated(EnumType.STRING) @Column(name = "tipoArte")
    private TipoArte tipoArte;

    public Exposicion() { 
        super(); setTipoEvento(TipoEvento.EXPOSICION); 
    }

    public Exposicion(String n, LocalDateTime fi, LocalDateTime ff, TipoArte t) {
        super(n, fi, ff, TipoEvento.EXPOSICION); 
        setTipoArte(t);
    }

    /** Reglas originales: máximo un curador. */
    public void asignarCurador(Persona persona) {
        if (persona == null) throw new IllegalArgumentException("Curador nulo.");
        if (contarPorRol(TipoRol.CURADOR) >= 1) throw new IllegalStateException("Ya hay curador.");
        agregarResponsable(persona, TipoRol.CURADOR);
    }

    @Override 
    protected boolean rolPermitido(TipoRol rol) { 
        return rol == TipoRol.ORGANIZADOR || rol == TipoRol.CURADOR; 
    }

    public TipoArte getTipoArte() { 
        return tipoArte; 
    }

    public void setTipoArte(TipoArte t) { 
        if (t == null) throw new IllegalArgumentException("TipoArte nulo"); 
        this.tipoArte = t; 
    }
    // nuevo helper que suelen llamar desde UI
    public Persona getCurador() {
        return obtenerResponsables(TipoRol.CURADOR).stream().findFirst().orElse(null);
    }
}
