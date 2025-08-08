package com.app_eventos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.app_eventos.model.enums.Modalidad;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.TipoRol;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.interfaces.IEventoConCupo;

public class Taller extends Evento implements IEventoConCupo {

    private int cupoMaximo;
    private final List<Persona> participantes = new ArrayList<>();
    private Modalidad modalidad;

    // Constructor con datos obligatorios
    public Taller(String nombre,
                  LocalDateTime fechaInicio,
                  LocalDateTime fechaFin,
                  int cupoMaximo,
                  Modalidad modalidad) {
        super(nombre, fechaInicio, fechaFin, TipoEvento.TALLER);
        setCupoMaximo(cupoMaximo);
        setModalidad(modalidad);
    }

    public Taller() {
        super();
        setTipoEvento(TipoEvento.TALLER);
    }

    // Implementación de IEventoConInscripcion
    @Override
    public void inscribirParticipante(Persona persona) {
        if (getEstado() != EstadoEvento.CONFIRMADO) {
            throw new IllegalStateException("El taller debe estar confirmado para inscribir participantes.");
        }
        if (participantes.size() >= cupoMaximo) {
            throw new IllegalStateException("Cupo lleno. No se pueden inscribir más participantes.");
        }
        if (participantes.contains(persona)) {
            throw new IllegalArgumentException("El participante ya está inscrito.");
        }
        participantes.add(persona);
    }

    @Override
    public void desinscribirParticipante(Persona persona) {
        participantes.remove(persona);
    }

    @Override
    public List<Persona> getParticipantes() {
        return new ArrayList<>(participantes);
    }

    // Implementación de IEventoConCupo
    @Override
    public int getCupoMaximo() {
        return cupoMaximo;
    }

    @Override
    public void setCupoMaximo(int cupoMaximo) {
        if (cupoMaximo <= 0) {
            throw new IllegalArgumentException("El cupo máximo debe ser mayor que cero.");
        }
        this.cupoMaximo = cupoMaximo;
    }

    @Override
    public int getCupoDisponible() {
        return cupoMaximo - participantes.size();
    }

    // Lógica de roles permitidos 
    @Override
    protected boolean rolPermitido(TipoRol rol) {
        return rol == TipoRol.INSTRUCTOR;
    }

    // Getters y setters propios 
    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        if (modalidad == null) {
            throw new IllegalArgumentException("La modalidad no puede ser nula.");
        }
        this.modalidad = modalidad;
    }
}