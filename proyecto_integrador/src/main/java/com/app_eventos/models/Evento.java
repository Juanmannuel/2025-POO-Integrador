package com.app_eventos.models;

import com.app_eventos.models.enums.EstadoEvento;
import com.app_eventos.models.enums.TipoEvento;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Evento {
    private Long idEvento;
    private TipoEvento tipoEvento;
    private String nombre;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Duration duracionEstimada;
    private EstadoEvento estado;
    private List<Persona> responsables;

    // Constructor
    public Evento(Long idEvento, TipoEvento tipoEvento, String nombre, String descripcion,
                  LocalDateTime fechaInicio, LocalDateTime fechaFin,
                  Duration duracionEstimada, EstadoEvento estado) {
        this.idEvento = idEvento;
        this.tipoEvento = tipoEvento;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.duracionEstimada = duracionEstimada;
        this.estado = estado;
        this.responsables = new ArrayList<>();
    }

    // Getters y Setters
    public Long getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(Long idEvento) {
        this.idEvento = idEvento;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
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

    public Duration getDuracionEstimada() {
        return duracionEstimada;
    }

    public void setDuracionEstimada(Duration duracionEstimada) {
        this.duracionEstimada = duracionEstimada;
    }

    public EstadoEvento getEstado() {
        return estado;
    }

    public void setEstado(EstadoEvento estado) {
        this.estado = estado;
    }

    public List<Persona> getResponsables() {
        return responsables;
    }

    public void setResponsables(List<Persona> responsables) {
        this.responsables = responsables;
    }
}