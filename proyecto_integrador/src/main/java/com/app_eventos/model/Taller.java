package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.*;
import com.app_eventos.model.interfaces.IEventoConCupo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "taller")
public class Taller extends Evento implements IEventoConCupo {

    @Column(name = "cupoMaximo", nullable = false)
    private int cupoMaximo;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad", nullable = false)
    private Modalidad modalidad;

    /** Tabla propia para participantes del taller. */
    @ManyToMany
    @JoinTable(
        name = "taller_participante",
        joinColumns = @JoinColumn(name = "evento_id"),
        inverseJoinColumns = @JoinColumn(name = "persona_id")
    )
    private List<Persona> participantes = new ArrayList<>();

    public Taller() {
        super();
        setTipoEvento(TipoEvento.TALLER);
    }

    public Taller(String n, LocalDateTime fi, LocalDateTime ff, int cupo, Modalidad m) {
        super(n, fi, ff, TipoEvento.TALLER);
        setCupoMaximo(cupo);
        setModalidad(m);
    }

    // --- Participantes ---
    @Override
    public void inscribirParticipante(Persona persona) {
        validarPuedeInscribir();

        if (personaTieneRol(persona))
            throw new IllegalStateException("No puede ser participante y responsable a la vez.");

        // ✅ Validación de cupo sin métodos booleanos
        if (getCupoDisponible() <= 0)
            throw new IllegalStateException("Cupo lleno.");

        if (participantes.contains(persona))
            throw new IllegalArgumentException("La persona ya está inscripta.");

        participantes.add(persona);
    }

    @Override public void desinscribirParticipante(Persona persona) { participantes.remove(persona); }
    @Override public List<Persona> getParticipantes() { return participantes; }

    // --- Cupo (sin booleanos) ---
    @Override public int getCupoMaximo() { return cupoMaximo; }

    @Override
    public void setCupoMaximo(int v) {
        // ⇨ Validación mínima aquí (sin mirar la colección lazy)
        if (v <= 0) throw new IllegalArgumentException("El cupo debe ser mayor a cero.");
        this.cupoMaximo = v;
    }


    @Override
    public int getCupoDisponible() {
        int disp = cupoMaximo - participantes.size();
        return Math.max(0, disp);
    }

    // --- Propios de Taller ---
    public void setModalidad(Modalidad m) {
        if (m == null) throw new IllegalArgumentException("Modalidad requerida.");
        this.modalidad = m;
    }
    public Modalidad getModalidad() { return modalidad; }

    // --- Roles permitidos / restricciones ---
    @Override
    protected boolean rolPermitido(TipoRol rol) {
        return rol == TipoRol.INSTRUCTOR || rol == TipoRol.ORGANIZADOR;
    }

    @Override
    protected void validarRestriccionesRol(TipoRol rol, Persona persona) {
        if (rol == TipoRol.INSTRUCTOR && contarPorRol(TipoRol.INSTRUCTOR) >= 1)
            throw new IllegalStateException("El taller solo admite un instructor.");
        if (participantes.contains(persona))
            throw new IllegalStateException("Ya es participante; no puede ser responsable.");
    }

    public void asignarInstructor(Persona persona) {
        if (persona == null) throw new IllegalArgumentException("Instructor nulo.");
        agregarResponsable(persona, TipoRol.INSTRUCTOR);
    }
}
