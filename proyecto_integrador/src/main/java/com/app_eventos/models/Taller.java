package com.app_eventos.models;

import com.app_eventos.models.enums.Modalidad;
import com.app_eventos.models.enums.TipoInscripcion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Taller extends Evento implements IEventoConInscripcion, IEventoConCupo {
    private Modalidad modalidad;
    private TipoInscripcion tipoInscripcion;
    private Persona curador;
    private List<Persona> responsables;
    private int cupoMaximo;
    private List<Persona> inscriptos;

    // Constructor
    public Taller(int codigo, String nombre, String descripcion, LocalDate fechaInicio, LocalDate fechaFin,
                  Modalidad modalidad, TipoInscripcion tipoInscripcion, Persona curador, int cupoMaximo) {
        super(codigo, nombre, descripcion, fechaInicio, fechaFin);
        this.modalidad = modalidad;
        this.tipoInscripcion = tipoInscripcion;
        this.curador = curador;
        this.cupoMaximo = cupoMaximo;
        this.inscriptos = new ArrayList<>();
        this.responsables = new ArrayList<>();
    }

    // Getters y Setters
    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public TipoInscripcion getTipoInscripcion() {
        return tipoInscripcion;
    }

    public void setTipoInscripcion(TipoInscripcion tipoInscripcion) {
        this.tipoInscripcion = tipoInscripcion;
    }

    public Persona getCurador() {
        return curador;
    }

    public void setCurador(Persona curador) {
        this.curador = curador;
    }

    public List<Persona> getResponsables() {
        return responsables;
    }

    public void setResponsables(List<Persona> responsables) {
        this.responsables = responsables;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(int cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public List<Persona> getInscriptos() {
        return inscriptos;
    }

    // Métodos de las interfaces
    @Override
    public void inscribirPersona(Persona persona) {
        if (inscriptos.size() < cupoMaximo && !inscriptos.contains(persona)) {
            inscriptos.add(persona);
        }
    }

    @Override
    public boolean hayCupoDisponible() {
        return inscriptos.size() < cupoMaximo;
    }
}