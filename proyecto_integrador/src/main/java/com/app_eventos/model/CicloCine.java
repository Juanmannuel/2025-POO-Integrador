package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.*;
import com.app_eventos.model.interfaces.IEventoConCupo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Ciclo de cine con cupo, películas (solo memoria) y participantes persistidos (ManyToMany). */
@Entity
@Table(name = "cicloCine")
public class CicloCine extends Evento implements IEventoConCupo {

    // Películas: siguen en memoria; si luego querés persistir, se arma otra tabla puente.
    @Transient
    private final List<Pelicula> peliculas = new ArrayList<>();

    /** Participantes persistidos en tabla puente evento_participante. */
    @ManyToMany
    @JoinTable(
        name = "evento_participante",
        joinColumns = @JoinColumn(name = "evento_id"),
        inverseJoinColumns = @JoinColumn(name = "persona_id")
    )
    private List<Persona> participantes = new ArrayList<>();

    @Column(name = "postCharla", nullable = false)
    private boolean postCharla;

    @Column(name = "cupoMaximo", nullable = false)
    private int cupoMaximo;

    public CicloCine() { super(); setTipoEvento(TipoEvento.CICLO_CINE); }

    public CicloCine(String n, LocalDateTime fi, LocalDateTime ff, boolean pc, int cupo) {
        super(n, fi, ff, TipoEvento.CICLO_CINE);
        setPostCharla(pc);
        setCupoMaximo(cupo);
    }

    // Películas
    public void agregarPelicula(Pelicula p) { if (p == null) throw new IllegalArgumentException("Película nula"); peliculas.add(p); }
    public void sacarPelicula(Pelicula p) { peliculas.remove(p); }
    public void clearPeliculas() { peliculas.clear(); }
    public List<Pelicula> getPeliculas() { return new ArrayList<>(peliculas); }

    // Participantes
    @Override
    public void inscribirParticipante(Persona persona) {
        validarPuedeInscribir();
        // NO permitir participante si ya es responsable en este evento
        if (personaTieneRol(persona))
            throw new IllegalStateException("No puede ser participante y responsable a la vez.");
        if (participantes.size() >= cupoMaximo) throw new IllegalStateException("Cupo completo.");
        if (participantes.contains(persona)) throw new IllegalArgumentException("La persona ya está inscripta.");
        participantes.add(persona);
    }

    @Override public void desinscribirParticipante(Persona persona) { participantes.remove(persona); }
    @Override public List<Persona> getParticipantes() { return participantes; }

    // Cupo
    @Override public boolean hayCupoDisponible() { return participantes.size() < cupoMaximo; }
    @Override public boolean tieneCupoDisponible() { return hayCupoDisponible(); }
    @Override public int getCupoMaximo() { return cupoMaximo; }
    @Override public void setCupoMaximo(int v) { if (v <= 0) throw new IllegalArgumentException("Cupo > 0"); this.cupoMaximo = v; }
    @Override public int getCupoDisponible() { return Math.max(0, cupoMaximo - participantes.size()); }

    // Roles
    @Override
    protected boolean rolPermitido(TipoRol rol) { return rol == TipoRol.ORGANIZADOR; }

    /** Bloquear asignar rol si ya es participante. */
    @Override
    protected void validarRestriccionesRol(TipoRol rol, Persona persona) {
        if (participantes.contains(persona))
            throw new IllegalStateException("Ya es participante; no puede ser responsable.");
    }

    // Props
    public boolean isPostCharla() { return postCharla; }
    public void setPostCharla(boolean v) { this.postCharla = v; }
}