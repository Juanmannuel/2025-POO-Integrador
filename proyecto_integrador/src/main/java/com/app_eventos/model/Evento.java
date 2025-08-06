package com.app_eventos.model;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
                .filter(rol -> rol.getRol() == TipoRol.ORGANIZADOR)
                .map(RolEvento::getPersona)
                .toList();
    }

    public Duration getDuracionEstimada() {
        return Duration.between(fechaInicio, fechaFin);
    }
    
    // FACTORY METHOD - Lógica de negocio para crear eventos según tipo
    public static Evento crearEvento(TipoEvento tipo, String nombre, 
                                   LocalDate fechaInicio, LocalDate fechaFin) {
        // Crear instancia del tipo específico
        Evento evento = switch (tipo) {
            case CONCIERTO -> new Concierto();
            case TALLER -> new Taller();
            case EXPOSICION -> new Exposicion();
            case FERIA -> new Feria();
            case CICLO_CINE -> new CicloCine();
        };
        
        // Configurar datos comunes
        evento.setNombre(nombre);
        evento.setFechaInicio(fechaInicio.atStartOfDay());
        evento.setFechaFin(fechaFin.atStartOfDay());
        evento.setTipoEvento(tipo);
        evento.setEstado(EstadoEvento.PLANIFICACIÓN);
        
        return evento;
    }
    
    // Lógica de negocio para cambio de estado
    public void confirmarEvento() {
        if (this.fechaInicio.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("No se puede confirmar un evento con fecha pasada");
        }
        this.estado = EstadoEvento.CONFIRMADO;
    }
    
    public void iniciarEvento() {
        if (this.estado != EstadoEvento.CONFIRMADO) {
            throw new IllegalStateException("Solo se pueden iniciar eventos confirmados");
        }
        this.estado = EstadoEvento.EJECUCION;
    }
    
    public void finalizarEvento() {
        if (this.estado != EstadoEvento.EJECUCION) {
            throw new IllegalStateException("Solo se pueden finalizar eventos en ejecución");
        }
        this.estado = EstadoEvento.FINALIZADO;
    }
    
    public void cancelarEvento() {
        if (this.estado == EstadoEvento.FINALIZADO) {
            throw new IllegalStateException("No se puede cancelar un evento finalizado");
        }
        this.estado = EstadoEvento.CANCELADO;
    }
    
    // Lógica de negocio para validar inscripciones
    public void validarInscripcion(Persona persona) {
        if (this.estado != EstadoEvento.CONFIRMADO) {
            throw new IllegalStateException("Solo se puede inscribir a eventos confirmados");
        }
        if (this.estado == EstadoEvento.FINALIZADO) {
            throw new IllegalStateException("No se puede inscribir a eventos finalizados");
        }
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
