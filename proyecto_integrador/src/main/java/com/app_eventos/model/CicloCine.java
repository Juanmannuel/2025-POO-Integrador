package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.*;
import com.app_eventos.model.interfaces.IEventoConCupo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Ciclo de cine con cupo, películas asociadas y participantes. */
@Entity
@Table(name = "cicloCine")
public class CicloCine extends Evento implements IEventoConCupo {

    /**
     * ✅ PELÍCULAS: persistidas con una tabla puente propia.
     */
    @ManyToMany
    @JoinTable(
        name = "ciclo_cine_peliculas",                                          // tabla intermedia
        joinColumns = @JoinColumn(name = "ciclo_id", referencedColumnName = "idEvento"),      // CAMBIO: referencia explícita a PK de Evento
        inverseJoinColumns = @JoinColumn(name = "pelicula_id", referencedColumnName = "idPelicula") // CAMBIO: referencia explícita a PK de Pelicula
    )
    private List<Pelicula> peliculas = new ArrayList<>();

    /** Participantes persistidos. */
    @ManyToMany
    @JoinTable(
        name = "cine_participante",
        joinColumns = @JoinColumn(name = "evento_id"),
        inverseJoinColumns = @JoinColumn(name = "persona_id")
    )
    private List<Persona> participantes = new ArrayList<>();

    @Column(name = "postCharla", nullable = false)
    private boolean postCharla;

    @Column(name = "cupoMaximo", nullable = false)
    private int cupoMaximo;

    public CicloCine() {
        super();
        setTipoEvento(TipoEvento.CICLO_CINE);
    }

    public CicloCine(String n, LocalDateTime fi, LocalDateTime ff, boolean pc, int cupo) {
        super(n, fi, ff, TipoEvento.CICLO_CINE);
        setPostCharla(pc);
        setCupoMaximo(cupo);
    }

    // ===== Películas (mismos métodos que ya usabas) =====
    public void agregarPelicula(Pelicula p) {
        if (p == null) throw new IllegalArgumentException("Película nula");
        if (!peliculas.contains(p)) peliculas.add(p);
    }

    public void sacarPelicula(Pelicula p) { peliculas.remove(p); }

    public void clearPeliculas() { peliculas.clear(); }

    /** Devuelve una copia para no exponer la colección interna. */
    public List<Pelicula> getPeliculas() { return new ArrayList<>(peliculas); }

    // ===== Participantes =====
    @Override
    public void inscribirParticipante(Persona persona) {
        validarPuedeInscribir();
        if (personaTieneRol(persona))
            throw new IllegalStateException("No puede ser participante y responsable a la vez.");

        if (getCupoDisponible() <= 0)
            throw new IllegalStateException("Cupo completo.");

        if (participantes.contains(persona))
            throw new IllegalArgumentException("La persona ya está inscripta.");

        participantes.add(persona);
    }

    @Override public void desinscribirParticipante(Persona persona) { participantes.remove(persona); }
    @Override public List<Persona> getParticipantes() { return participantes; }

    // ===== Cupo =====
    @Override public int getCupoMaximo() { return cupoMaximo; }

    @Override
    public void setCupoMaximo(int v) {
        if (v <= 0) throw new IllegalArgumentException("El cupo debe ser mayor a 0.");
        this.cupoMaximo = v;
    }

    @Override
    public int getCupoDisponible() {
        int disp = cupoMaximo - participantes.size();
        return Math.max(0, disp);
    }

    // ===== Roles =====
    @Override protected boolean rolPermitido(TipoRol rol) { return rol == TipoRol.ORGANIZADOR; }

    @Override
    protected void validarRestriccionesRol(TipoRol rol, Persona persona) {
        if (participantes.contains(persona))
            throw new IllegalStateException("Ya es participante; no puede ser responsable.");
    }

    // ===== Get/Set extra =====
    public boolean isPostCharla() { return postCharla; }
    public void setPostCharla(boolean v) { this.postCharla = v; }
}
