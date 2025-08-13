package com.app_eventos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import jakarta.persistence.*;
import com.app_eventos.model.enums.*;

/**
 * Entidad base de todos los eventos.
 * - NO maneja participantes (eso lo hacen los subtipos que permiten inscripción).
 * - Maneja la relación con roles (RolEvento).
 */
@Entity
@Table(name = "evento")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEvento")
    private Long idEvento;

    @Column(nullable = false)
    private String nombre;

    @Column
    private LocalDateTime fechaInicio;

    @Column
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEvento estado = EstadoEvento.PLANIFICACIÓN;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipoEvento", nullable = false)
    private TipoEvento tipoEvento;

    /** Relación con roles. OrphanRemoval para borrar el rol si se saca del evento. */
    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<RolEvento> roles = new ArrayList<>();

    protected Evento() {
        this.estado = EstadoEvento.PLANIFICACIÓN;
    }

    public Evento(String nombre, LocalDateTime fechaInicio, LocalDateTime fechaFin, TipoEvento tipoEvento) {
        setNombre(nombre);
        setFechaInicio(fechaInicio);
        setFechaFin(fechaFin);
        setTipoEvento(tipoEvento);
        this.estado = EstadoEvento.PLANIFICACIÓN;
    }

    // --------- Reglas de estado ----------
    public void cambiarEstado(EstadoEvento nuevoEstado) {
        LocalDateTime now = LocalDateTime.now();
        if (nuevoEstado == EstadoEvento.CONFIRMADO && this.fechaInicio.isBefore(now))
            throw new IllegalStateException("No se puede confirmar con fecha pasada.");
        if (nuevoEstado == EstadoEvento.EJECUCIÓN && (now.isBefore(fechaInicio) || now.isAfter(fechaFin)))
            throw new IllegalStateException("Solo en ejecución entre inicio y fin.");
        if (nuevoEstado == EstadoEvento.FINALIZADO && now.isBefore(fechaFin))
            throw new IllegalStateException("No puede finalizar antes del fin.");
        this.estado = nuevoEstado;
    }

    /** Ventana de inscripción por defecto: evento confirmado y no finalizado. */
    public boolean Inscripcion() {
        LocalDateTime now = LocalDateTime.now();
        return this.estado == EstadoEvento.CONFIRMADO && now.isBefore(getFechaFin());
    }

    /** Auto–actualiza a FINALIZADO si corresponde. */
    public void verificarEstadoAutomatico() {
        LocalDateTime now = LocalDateTime.now();
        if ((this.estado == EstadoEvento.EJECUCIÓN || this.estado == EstadoEvento.CONFIRMADO)
                && now.isAfter(this.fechaFin)) {
            this.estado = EstadoEvento.FINALIZADO;
        }
    }

    protected void validarPuedeInscribir() {
        if (!Inscripcion()) throw new IllegalStateException("No se permite inscribir.");
    }

    /** Gancho para validaciones específicas de subtipos cuando asignan roles. */
    protected void validarRestriccionesRol(TipoRol rol, Persona persona) {
        // Por defecto, sin restricciones extra
    }

    // --------- Roles ----------
    /**
     * Asigna un rol a una persona.
     * Regla: una persona no puede tener más de UN rol en el mismo evento.
     */
    public void agregarResponsable(Persona persona, TipoRol rol) {
        if (persona == null || rol == null) throw new IllegalArgumentException("Persona y rol requeridos.");
        if (!rolPermitido(rol)) throw new IllegalArgumentException("Rol no permitido para " + tipoEvento);

        // Regla global: una persona no puede tener más de un rol en el evento
        boolean yaTieneAlguno = roles.stream().anyMatch(r -> r.getPersona().equals(persona));
        if (yaTieneAlguno) throw new IllegalArgumentException("La persona ya tiene un rol asignado en este evento.");

        // Validaciones específicas del subtipo (instructor único, participante/rol, etc.)
        validarRestriccionesRol(rol, persona);

        this.roles.add(new RolEvento(this, persona, rol));
    }

    public void borrarResponsable(Persona persona, TipoRol rol) {
        if (persona == null || rol == null) return;
        roles.removeIf(r -> r.getPersona().equals(persona) && r.getRol() == rol);
    }

    public List<Persona> obtenerResponsables(TipoRol rol) {
        return roles.stream()
                .filter(r -> r.getRol() == rol)
                .map(RolEvento::getPersona)
                .toList();
    }

    public boolean personaTieneRol(Persona persona) {
        return roles.stream().anyMatch(r -> r.getPersona().equals(persona));
    }

    public long contarPorRol(TipoRol rol) {
        return roles.stream().filter(r -> r.getRol() == rol).count();
    }

    /** Ejemplo de invariante: al menos un organizador. */
    public void validarInvariantes() {
        if (contarPorRol(TipoRol.ORGANIZADOR) == 0)
            throw new IllegalStateException("Todo evento debe tener al menos un organizador.");
    }

    /** Cada subtipo define qué roles permite. */
    protected abstract boolean rolPermitido(TipoRol rol);

    public EnumSet<TipoRol> rolesPermitidosParaAsignacion() {
        EnumSet<TipoRol> set = EnumSet.noneOf(TipoRol.class);
        for (TipoRol r : TipoRol.values()) if (rolPermitido(r)) set.add(r);
        return set;
    }

    /** Útil si se quiere inyectar un rol ya creado (merge/attach). */
    public void agregarRol(RolEvento rol) {
        if (rol != null) roles.add(rol);
    }

    // --------- Getters / Setters ----------
    public Long getIdEvento() { return idEvento; }
    public void setIdEvento(Long idEvento) { this.idEvento = idEvento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre vacío.");
        this.nombre = nombre;
    }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) {
        if (fechaInicio == null) throw new IllegalArgumentException("Inicio nulo.");
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) {
        if (fechaFin == null) throw new IllegalArgumentException("Fin nulo.");
        if (this.fechaInicio != null && fechaFin.isBefore(this.fechaInicio))
            throw new IllegalArgumentException("Fin antes que inicio.");
        this.fechaFin = fechaFin;
    }

    public EstadoEvento getEstado() { return estado; }
    public void setEstado(EstadoEvento estado) {
        if (estado == null) throw new IllegalArgumentException("Estado nulo.");
        this.estado = estado;
    }

    public TipoEvento getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(TipoEvento tipoEvento) {
        if (tipoEvento == null) throw new IllegalArgumentException("Tipo de evento nulo.");
        this.tipoEvento = tipoEvento;
    }

    /** Devuelve copia para no exponer la lista interna. */
    public List<RolEvento> getRoles() { return new ArrayList<>(roles); }
}