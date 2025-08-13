package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.*;
import com.app_eventos.model.interfaces.IEventoConCupo;
import com.app_eventos.model.interfaces.IEventoConInscripcion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Taller con cupo, modalidad y participantes persistidos. */
@Entity @Table(name = "taller")
public class Taller extends Evento implements IEventoConCupo, IEventoConInscripcion {

    @Column(name = "cupoMaximo", nullable = false)
    private int cupoMaximo;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad", nullable = false)
    private Modalidad modalidad;

    @ManyToMany
    @JoinTable(
        name = "taller_participante",
        joinColumns = @JoinColumn(name = "evento_id"),
        inverseJoinColumns = @JoinColumn(name = "persona_id")
    )
    private List<Persona> participantes = new ArrayList<>();

    public Taller() { super(); setTipoEvento(TipoEvento.TALLER); }
    public Taller(String n, LocalDateTime fi, LocalDateTime ff, int cupo, Modalidad m) {
        super(n, fi, ff, TipoEvento.TALLER); setCupoMaximo(cupo); setModalidad(m);
    }

    @Override public void inscribirParticipante(Persona persona) {
        validarPuedeInscribir();
        if (participantes.size() >= cupoMaximo) throw new IllegalStateException("Cupo lleno.");
        if (participantes.contains(persona)) throw new IllegalArgumentException("La persona ya está inscripta.");
        participantes.add(persona);
    }
    @Override public void desinscribirParticipante(Persona persona) { participantes.remove(persona); }
    @Override public List<Persona> getParticipantes() { return participantes; }

    @Override public boolean hayCupoDisponible() { return participantes.size() < cupoMaximo; }
    @Override public boolean tieneCupoDisponible() { return hayCupoDisponible(); }
    @Override public int getCupoMaximo() { return cupoMaximo; }
    @Override public void setCupoMaximo(int v) {
        if (v <= 0) throw new IllegalArgumentException("Cupo > 0");
        if (v < participantes.size()) throw new IllegalStateException("El cupo no puede ser menor a los inscriptos actuales.");
        this.cupoMaximo = v;
    }
    @Override public int getCupoDisponible() { return Math.max(0, cupoMaximo - participantes.size()); }

    public void setModalidad(Modalidad m) { if (m == null) throw new IllegalArgumentException("Modalidad requerida."); this.modalidad = m; }
    public Modalidad getModalidad() { return modalidad; }

    @Override protected boolean rolPermitido(TipoRol rol) { return rol == TipoRol.INSTRUCTOR || rol == TipoRol.ORGANIZADOR; }

    public void asignarInstructor(Persona persona) {
        if (persona == null) throw new IllegalArgumentException("Instructor nulo.");
        if (contarPorRol(TipoRol.INSTRUCTOR) >= 1) throw new IllegalStateException("Ya hay un instructor asignado.");
        agregarResponsable(persona, TipoRol.INSTRUCTOR);
    }
    public Persona getInstructor() { return obtenerResponsables(TipoRol.INSTRUCTOR).stream().findFirst().orElse(null); }
}
