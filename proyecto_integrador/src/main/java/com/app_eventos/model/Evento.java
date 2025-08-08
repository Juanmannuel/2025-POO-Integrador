package com.app_eventos.model;

import java.time.LocalDateTime;
import java.time.Duration;
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
    private List<RolEvento> roles = new ArrayList<>();

    // Constructor con TipoEvento
    public Evento(String nombre, LocalDateTime fechaInicio, LocalDateTime fechaFin, TipoEvento tipoEvento) {
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipoEvento = tipoEvento;
        this.estado = EstadoEvento.PLANIFICACIÓN;
    }

    // Constructor vacío
    public Evento() {
        this.estado = EstadoEvento.PLANIFICACIÓN;
    }

    // Métodos modelo rico
    public void cambiarEstado(EstadoEvento nuevoEstado) {
        if (nuevoEstado == EstadoEvento.CONFIRMADO && this.fechaInicio.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("No se puede confirmar un evento con fecha pasada.");
        }
        this.estado = nuevoEstado;
    }

    public void agregarResponsable(Persona persona) {
        RolEvento nuevoRol = new RolEvento(this, persona, TipoRol.ORGANIZADOR);
        this.roles.add(nuevoRol);
    }

    public void BorrarResponsable(Persona persona) {
        this.roles.removeIf(rol -> rol.getPersona().equals(persona) && rol.getRol() == TipoRol.ORGANIZADOR);
    }

    public List<Persona> obtenerResponsables() {
        return this.roles.stream()
                .filter(rol -> rol.getRol() == TipoRol.ORGANIZADOR && rol.estaActivo())
                .map(RolEvento::getPersona)
                .toList();
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



    public Duration getDuracionEstimada() {
        return Duration.between(fechaInicio, fechaFin);
    }

    public String descripcionDetallada() {
        return nombre + " (" + tipoEvento + ") - " +
                "Inicio: " + fechaInicio + ", Fin: " + fechaFin + ", Estado: " + estado;
    }

    // Getters y setters

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
        this.nombre = nombre;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public EstadoEvento getEstado() {
        return estado;
    }

    public void setEstado(EstadoEvento estado) {
        this.estado = estado;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public List<RolEvento> getRoles() {
        return roles;
    }

    public void setRoles(List<RolEvento> roles) {
        this.roles = roles;
    }
}
