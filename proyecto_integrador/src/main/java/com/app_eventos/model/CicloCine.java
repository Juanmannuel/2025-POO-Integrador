package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.*;
import com.app_eventos.model.interfaces.IEventoConCupo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Ciclo de cine con cupo y lista en memoria para pruebas. */
@Entity @Table(name = "cicloCine")
public class CicloCine extends Evento implements IEventoConCupo {

    // Solo memoria; si luego querés persistir, se arma tabla puente CicloCine_Pelicula.
    @Transient private final List<Pelicula> peliculas = new ArrayList<>();
    @Transient private final List<Persona> participantes = new ArrayList<>();

    @Column(name = "postCharla") private boolean postCharla;
    @Column(name = "cupoMaximo") private int cupoMaximo;

    public CicloCine() { super(); setTipoEvento(TipoEvento.CICLO_CINE); }
    public CicloCine(String n, LocalDateTime fi, LocalDateTime ff, boolean pc, int cupo) {
        super(n, fi, ff, TipoEvento.CICLO_CINE); setPostCharla(pc); setCupoMaximo(cupo);
    }

    // Películas en memoria
    public void agregarPelicula(Pelicula p) { if (p == null) throw new IllegalArgumentException("Película nula"); peliculas.add(p); }
    public void sacarPelicula(Pelicula p) { peliculas.remove(p); }
    public void clearPeliculas() { peliculas.clear(); }

    // Inscripciones en memoria
    @Override public void inscribirParticipante(Persona persona) {
        validarPuedeInscribir();
        if (participantes.size() >= cupoMaximo) throw new IllegalStateException("Cupo lleno.");
        if (participantes.contains(persona)) throw new IllegalArgumentException("Ya inscrito.");
        participantes.add(persona);
    }
    @Override public void desinscribirParticipante(Persona persona) { participantes.remove(persona); }
    @Override public List<Persona> getParticipantes() { return new ArrayList<>(participantes); }

    // Cupo
    @Override public boolean hayCupoDisponible() { return participantes.size() < cupoMaximo; }
    @Override public boolean tieneCupoDisponible() { return hayCupoDisponible(); }
    @Override public int getCupoMaximo() { return cupoMaximo; }
    @Override public void setCupoMaximo(int v) { if (v <= 0) throw new IllegalArgumentException("Cupo > 0"); this.cupoMaximo = v; }
    @Override public int getCupoDisponible() { return cupoMaximo - participantes.size(); }

    @Override protected boolean rolPermitido(TipoRol rol) { return rol == TipoRol.ORGANIZADOR; }

    public List<Pelicula> getPeliculas() { return new ArrayList<>(peliculas); }
    public boolean isPostCharla() { return postCharla; }
    public void setPostCharla(boolean v) { this.postCharla = v; }
}
