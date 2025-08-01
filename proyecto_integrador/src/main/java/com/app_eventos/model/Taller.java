package com.app_eventos.model;

import java.time.LocalDateTime;

import com.app_eventos.model.enums.Modalidad;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.interfaces.IEventoConCupo;
import com.app_eventos.model.interfaces.IEventoConInscripcion;

public class Taller extends Evento implements IEventoConCupo, IEventoConInscripcion {

    private int cupoMaximo;
    private int inscriptos;
    private Modalidad modalidad;
    private Persona instructor;

    // Constructor
    public Taller(String nombre, 
                  LocalDateTime fechaInicio, 
                  LocalDateTime fechaFin, 
                  int cupoMaximo, 
                  Modalidad modalidad) {
        super(nombre, fechaInicio, fechaFin, TipoEvento.TALLER);
        this.cupoMaximo = cupoMaximo;
        this.modalidad = modalidad;
        this.inscriptos = 0;
    }

    public Taller() {
        super();
        this.setTipoEvento(TipoEvento.TALLER);
        this.inscriptos = 0;
    }

    // Implementación lógica (modelo RICO)

    @Override
    public void inscribir(Persona participante) {
        if (getEstado() != EstadoEvento.CONFIRMADO) {
            throw new IllegalStateException("No se puede inscribir. El taller no está confirmado.");
        }
        if (inscriptos >= cupoMaximo) {
            throw new IllegalStateException("Cupo lleno. No se puede inscribir más participantes.");
        }
        inscriptos++;
    }

    @Override
    public boolean hayCupoDisponible() {
        return inscriptos < cupoMaximo;
    }

    // Lógica para asignar instructor

    public void asignarInstructor(Persona persona) {
        if (this.instructor != null) {
            throw new IllegalStateException("Ya hay un instructor asignado.");
        }
        this.instructor = persona;
    }

    public void quitarInstructor() {
        this.instructor = null;
    }

    // Getters y setters

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(int cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public int getInscriptos() {
        return inscriptos;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public Persona getInstructor() {
        return instructor;
    }

    public void setInstructor(Persona instructor) {
        this.instructor = instructor;
    }
}
