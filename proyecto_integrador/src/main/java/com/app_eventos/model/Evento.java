package com.app_eventos.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.TipoRol;

public abstract class Evento {

    private Long idEvento;
    private String nombre;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private EstadoEvento estado;
    private TipoEvento tipoEvento;

    // Lista de relaciones Evento–Persona–Rol
    private final List<RolEvento> roles = new ArrayList<>();

    // Constructor con datos obligatorios
    public Evento(String nombre, LocalDateTime fechaInicio, LocalDateTime fechaFin, TipoEvento tipoEvento) {
        setNombre(nombre);
        setFechaInicio(fechaInicio);
        setFechaFin(fechaFin);
        setTipoEvento(tipoEvento);
        this.estado = EstadoEvento.PLANIFICACIÓN;
    }

    // Constructor vacío
    public Evento() {
        this.estado = EstadoEvento.PLANIFICACIÓN;
    }

    // ====== MÉTODOS DE NEGOCIO ======

    // Cambia el estado del evento con validaciones de negocio.

    public void cambiarEstado(EstadoEvento nuevoEstado) {
        if (nuevoEstado == EstadoEvento.CONFIRMADO && this.fechaInicio.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("No se puede confirmar un evento con fecha pasada.");
        }
        this.estado = nuevoEstado;
    }

    // Agrega un responsable al evento validando el rol permitido para esta subclase.
    public void agregarResponsable(Persona persona, TipoRol rol) {
        if (persona == null || rol == null) throw new IllegalArgumentException("Persona y rol no pueden ser nulos.");
        if (!rolPermitido(rol)) throw new IllegalArgumentException("Rol " + rol + " no permitido para " + tipoEvento);

        boolean existe = roles.stream()
                .anyMatch(r -> r.getPersona().equals(persona) && r.getRol() == rol);
        if (existe) throw new IllegalArgumentException("La persona ya tiene el rol " + rol + " en este evento.");

        this.roles.add(new RolEvento(this, persona, rol));
    }

    // Elimina un responsable con un rol específico.
 
    public void borrarResponsable(Persona persona, TipoRol rol) {
        this.roles.removeIf(r -> r.getPersona().equals(persona) && r.getRol() == rol);
    }

    // Devuelve una lista filtrada de responsables según rol.
    public List<Persona> obtenerResponsables(TipoRol rol) {
        return this.roles.stream()
                .filter(r -> r.getRol() == rol)
                .map(RolEvento::getPersona)
                .toList();
    }

    // Devuelve todos los responsables sin filtrar.
    public List<RolEvento> obtenerTodosLosRoles() {
        return new ArrayList<>(roles);
    }

    // ⭐ MÉTODOS DE NEGOCIO PARA PARTICIPACIONES

    /**
     * Verifica si el evento puede recibir inscripciones
     */
    public boolean puedeInscribirParticipantes() {
        return this.estado == EstadoEvento.CONFIRMADO;
    }

    /**
     * Verifica si una persona ya tiene un rol en este evento
     */
    public boolean personaTieneRol(Persona persona) {
        return this.roles.stream()
                .anyMatch(rol -> rol.getPersona().equals(persona) && rol.estaActivo());
    }

    /**
     * Obtiene el rol activo de una persona en este evento
     */
    public Optional<RolEvento> obtenerRolPersona(Persona persona) {
        return this.roles.stream()
                .filter(rol -> rol.getPersona().equals(persona) && rol.estaActivo())
                .findFirst();
    }

    /**
     * Cuenta los participantes activos en el evento
     */
    public long contarParticipantesActivos() {
        return this.roles.stream()
                .filter(rol -> rol.esParticipante() && rol.estaActivo())
                .count();
    }

    /**
     * Obtiene todos los roles activos del evento
     */
    public List<RolEvento> obtenerRolesActivos() {
        return this.roles.stream()
                .filter(RolEvento::estaActivo)
                .toList();
    }

    /**
     * Agrega un rol al evento (usado internamente)
     */
    public void agregarRol(RolEvento rol) {
        this.roles.add(rol);
    }

    // Calcula la duración estimada del evento.
    public Duration getDuracionEstimada() {
        return Duration.between(fechaInicio, fechaFin);
    }

    // Descripción básica del evento.
    public String descripcionDetallada() {
        return nombre + " (" + tipoEvento + ") - " +
                "Inicio: " + fechaInicio + ", Fin: " + fechaFin + ", Estado: " + estado;
    }

    // Método que cada subclase debe implementar para validar los roles permitidos.
    protected abstract boolean rolPermitido(TipoRol rol);

    // GETTERS & SETTERS
    public Long getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(Long idEvento) {
        this.idEvento = idEvento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        this.nombre = nombre;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        if (fechaInicio == null) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser nula.");
        }
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        if (fechaFin == null) {
            throw new IllegalArgumentException("La fecha de fin no puede ser nula.");
        }
        if (this.fechaInicio != null && fechaFin.isBefore(this.fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
        this.fechaFin = fechaFin;
    }

    public EstadoEvento getEstado() {
        return estado;
    }

    public void setEstado(EstadoEvento estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo.");
        }
        this.estado = estado;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEvento tipoEvento) {
        if (tipoEvento == null) {
            throw new IllegalArgumentException("El tipo de evento no puede ser nulo.");
        }
        this.tipoEvento = tipoEvento;
    }

    public List<RolEvento> getRoles() {
        return new ArrayList<>(roles);
    }

    public List<Persona> obtenerResponsables() {
        return roles.stream()
                .map(RolEvento::getPersona)
                .toList();
    }
}
